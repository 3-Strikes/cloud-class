package com.example.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.api.MediaServiceAPI;
import com.example.constant.Constants;
import com.example.doc.CourseDoc;
import com.example.domain.*;
import com.example.dto.CourseDTO;
import com.example.mapper.*;
import com.example.result.JSONResult;
import com.example.service.*;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.vo.CourseDetailVO;
import com.example.vo.CourseOrderVO;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author lzy
 * @since 2025-12-08
 */
@Service
public class CourseServiceImpl extends ServiceImpl<CourseMapper, Course> implements CourseService {
    @Autowired
    private TeacherService teacherService;
    @Autowired
    private CourseDetailService detailService;
    @Autowired
    private CourseMarketService marketService;
    @Autowired
    private CourseResourceService resourceService;
    @Autowired
    private CourseSummaryService summaryService;
    @Autowired
    private CourseChapterService chapterService;

    @Autowired
    private CourseDetailMapper courseDetailMapper;
    @Autowired
    private CourseMarketMapper courseMarketMapper;
    @Autowired
    private CourseResourceMapper courseResourceMapper;
    @Autowired
    private TeacherMapper teacherMapper;
    @Autowired
    private CourseChapterMapper courseChapterMapper;
    
    @Autowired
    private RocketMQTemplate mqTemplate;
    @Autowired
    private MediaServiceAPI mediaServiceAPI;
    //本地事务
    @Transactional
    @Override
    public void saveCourseDTO(CourseDTO courseDTO) {
        //根据讲师id查询讲师名称
        List<Long> teacharIds = courseDTO.getTeacharIds();
        List<String> names=teacherService.selectNamesByIds(teacharIds);
        //保存课程信息
        Course course = courseDTO.getCourse();
        course.setStatus(0);
        course.setLoginId(0L);
        course.setLoginUserName("test");
        course.setTeacherNames(CollectionUtil.join(names, ","));
        save(course);
        //保存课程详情信息
        CourseDetail courseDetail = courseDTO.getCourseDetail();
        courseDetail.setId(course.getId());
        detailService.save(courseDetail);
        //保存课程营销信息
        CourseMarket courseMarket = courseDTO.getCourseMarket();
        courseMarket.setId(course.getId());
        if(courseMarket.getPrice()==null)courseMarket.setPrice(0.0);
        if(courseMarket.getPriceOld()==null)courseMarket.setPriceOld(0.0);
        marketService.save(courseMarket);

        //保存课程资源信息
        CourseResource courseResource = courseDTO.getCourseResource();
        courseResource.setCourseId(course.getId());
        resourceService.save(courseResource);
    }

    @Override
    public CourseDTO getCourseDTOById(Long id) {
        CourseDTO dto = new CourseDTO();

        // 1. 课程基本信息
        Course course = this.getById(id);
        if (course == null) {
            throw new RuntimeException("课程不存在");
        }
        dto.setCourse(course);

        // 2. 课程详情
        CourseDetail detail = courseDetailMapper.selectOne(
                new LambdaQueryWrapper<CourseDetail>().eq(CourseDetail::getId, id));
        dto.setCourseDetail(detail);

        // 3. 课程营销信息
        CourseMarket market = courseMarketMapper.selectOne(
                new LambdaQueryWrapper<CourseMarket>().eq(CourseMarket::getId, id));
        dto.setCourseMarket(market);

        // 4. 课程资源
        CourseResource resource = courseResourceMapper.selectOne(
                new LambdaQueryWrapper<CourseResource>().eq(CourseResource::getCourseId, id));
        dto.setCourseResource(resource);

        // 5. 授课老师ID列表
        List<CourseTeacher> courseTeachers = teacherMapper.selectList(
                new LambdaQueryWrapper<CourseTeacher>().eq(CourseTeacher::getCourseId, id));
        dto.setTeacharIds(courseTeachers.stream().map(CourseTeacher::getTeacherId).collect(Collectors.toList()));

        return dto;
    }

    @Override
    public void updateCourseChapterCount(Long courseId) {
        // 1. 查询该课程的章节总数
        Integer chapterCount = courseChapterMapper.countByCourseId(courseId);

        // 2. 更新课程表的章节数字段
        Course course = new Course();
        course.setId(courseId);
        course.setChapterCount(chapterCount);
        this.updateById(course);
    }

    @Override
    public List<CourseDoc> listCourseDoc(Set<Long> ids) {
        if(CollectionUtil.isEmpty(ids)) return null;
        List<CourseDoc> result=new ArrayList<>();

        //查询课程基本信息
        List<Course> courseList = this.list(Wrappers.lambdaQuery(Course.class).in(Course::getId,ids));
        //查询课程价格信息
        List<CourseMarket> list = marketService.list(Wrappers.lambdaQuery(CourseMarket.class).in(CourseMarket::getId, ids));
        Map<Long, CourseMarket> marketMap = getMarketMap(list);
        //查询课程统计信息
        List<CourseSummary> summaryList = summaryService.list(Wrappers.lambdaQuery(CourseSummary.class).in(CourseSummary::getId, ids));
        Map<Long, CourseSummary> summaryMap = getSummaryMap(summaryList);

        //封装数据CourseDoc
        for (Course course : courseList) {
            CourseDoc doc=new CourseDoc();
            BeanUtils.copyProperties(course,doc);

            CourseMarket courseMarket = marketMap.get(course.getId());
            BeanUtils.copyProperties(courseMarket,doc);

            CourseSummary courseSummary = summaryMap.get(course.getId());
            BeanUtils.copyProperties(courseSummary,doc);
            result.add(doc);
        }

        return result;
    }

    @Override
    public void sendPuslishMessage(List<CourseDoc> courseDocList) {
        //取集合中前2个课程，得到课程名 <a href="ssss.html?courseid=xxx">
        StringBuffer sb=new StringBuffer();
        String contents="尊敬的会员，平台新上课程[";
        if(courseDocList.size()>=2){
            for(int i=0;i<2;i++){
                CourseDoc courseDoc = courseDocList.get(i);
                String name = courseDoc.getName();
                sb.append(name+",");
            }
            contents=sb.substring(0,sb.length()-1)+"...";
        }else{
            for (CourseDoc courseDoc : courseDocList) {
                String name = courseDoc.getName();
                sb.append(name+",");
            }
            contents=sb.substring(0,sb.length()-1);
        }
        contents+="]已上架，请登录平台查看";

        String topic = Constants.PUBLISH_COURSE_TOPIC;
        MessageStation messageStation=new MessageStation();
        messageStation.setTitle("新课程发布了");
        messageStation.setContent(contents);
        messageStation.setType("系统消息");
        messageStation.setSendTime(new Date());
        messageStation.setIsread(0);
        mqTemplate.sendOneWay(topic+":"+Constants.STATION_TAGS,messageStation);


        MessageSms sms=new MessageSms();
        sms.setTitle("新课程发布了");
        sms.setContent(contents);
        sms.setSendTime(new Date());
        mqTemplate.sendOneWay(topic+":"+Constants.SMS_TAGS,sms);

        MessageEmail email=new MessageEmail();
        email.setTitle("新课程发布了");
        email.setContent(contents);
        email.setSendTime(new Date());
        mqTemplate.sendOneWay(topic+":"+Constants.EMAIL_TAGS,email);
    }

    @Override
    public CourseOrderVO getCourseInfoByIds(List<Long> courseIds) {
        //参数校验
        if(CollectionUtil.isEmpty(courseIds)) return new CourseOrderVO(0.0,Collections.emptyList());
        CourseOrderVO result=new CourseOrderVO();
        List<Course> courses = this.listByIds(courseIds);
        if(CollectionUtil.isEmpty(courses)) return new CourseOrderVO(0.0,Collections.emptyList());
        List<CourseMarket> courseMarkets = marketService.listByIds(courseIds);
        Double totalAmount=0.0;

        List<CourseOrderVO.CourseAndMarket> courseInfos=new ArrayList<>();
        for (Course cours : courses) {
            CourseOrderVO.CourseAndMarket courseAndMarket=new CourseOrderVO.CourseAndMarket();
            CourseMarket cm = courseMarkets.stream().filter(courseMarket -> courseMarket.getId().equals(cours.getId())).findFirst().get();

            totalAmount+=cm.getPrice();
            courseAndMarket.setCourse(cours);
            courseAndMarket.setCourseMarket(cm);
            courseInfos.add(courseAndMarket);
        }

        result.setCourseInfos(courseInfos);
        result.setTotalAmount(totalAmount.doubleValue());
        return result;
    }

    @Override
    public CourseDetailVO getDetailById(Long courseId) {
        CourseDetailVO result=new CourseDetailVO();
        //课程基本信息
        result.setCourse(getById(courseId));
        //价格信息
        result.setCourseMarket(marketService.getById(courseId));
        //课程详情
        result.setCourseDetail(detailService.getById(courseId));
        //统计信息
        result.setCourseSummary(summaryService.getById(courseId));
        //讲师信息
        result.setTeachers(teacherService.listByCourseId(courseId));
        //章节信息（包含各章节视频信息）
        result.setCourseChapters(this.generateChapters(courseId));
        return result;
    }

    private List<CourseChapter> generateChapters(Long courseId) {
        //查询该课程下所有章节集合
        List<CourseChapter> list = chapterService.list(Wrappers.lambdaQuery(CourseChapter.class).eq(CourseChapter::getCourseId, courseId));

        JSONResult<List<MediaFile>> listJSONResult = mediaServiceAPI.listMediaFiles(courseId);
        List<MediaFile> mediaFiles = listJSONResult.getData();
        //key:章节id，value：视频集合
        Map<Long,List<MediaFile>> mediaFileMap=new HashMap<>();
        for (MediaFile mediaFile : mediaFiles) {
            mediaFile.setFileUrl(null);
            Long chapterId = mediaFile.getChapterId();
            if(mediaFileMap.containsKey(chapterId)){
                mediaFileMap.get(chapterId).add(mediaFile);
            }else{
                List<MediaFile> tmp=new ArrayList<>();
                tmp.add(mediaFile);
                mediaFileMap.put(chapterId,tmp);
            }
        }

        //把mediaFiles分到各章节
        for (CourseChapter courseChapter : list) {
            Long chapterId = courseChapter.getId();
            courseChapter.setMediaFiles(mediaFileMap.get(chapterId));
        }
        return list;
    }

//    @Override
//    public void sendPuslishMessage(List<CourseDoc> courseDocList) {
//        //取集合中前2个课程，得到课程名 <a href="ssss.html?courseid=xxx">
//        StringBuffer sb=new StringBuffer();
//        String contents="尊敬的会员，平台新上课程[";
//        if(courseDocList.size()>=2){
//            for(int i=0;i<2;i++){
//                CourseDoc courseDoc = courseDocList.get(i);
//                String name = courseDoc.getName();
//                sb.append(name+",");
//            }
//            contents=sb.substring(0,sb.length()-1)+"...";
//        }else{
//            for (CourseDoc courseDoc : courseDocList) {
//                String name = courseDoc.getName();
//                sb.append(name+",");
//            }
//            contents=sb.substring(0,sb.length()-1);
//        }
//        contents+="]已上架，请登录平台查看";
//
//        String topic = Constants.PUBLISH_COURSE_TOPIC;
//        MessageStation messageStation=new MessageStation();
//        messageStation.setTitle("新课程发布了");
//        messageStation.setContent(contents);
//        messageStation.setType("系统消息");
//        messageStation.setSendTime(new Date());
//        messageStation.setIsread(0);
//        mqTemplate.sendOneWay(topic+":"+Constants.STATION_TAGS,messageStation);
//
//
//        MessageSms sms=new MessageSms();
//        sms.setTitle("新课程发布了");
//        sms.setContent(contents);
//        sms.setSendTime(new Date());
//        mqTemplate.sendOneWay(topic+":"+Constants.SMS_TAGS,sms);
//
//        MessageEmail email=new MessageEmail();
//        email.setTitle("新课程发布了");
//        email.setContent(contents);
//        email.setSendTime(new Date());
//        mqTemplate.sendOneWay(topic+":"+Constants.EMAIL_TAGS,email);
//    }

    private Map<Long,CourseMarket> getMarketMap(List<CourseMarket> list) {
        Map<Long,CourseMarket> map=new HashMap<>();
        for (CourseMarket courseMarket : list) {
            map.put(courseMarket.getId(),courseMarket);
        }
        return map;
    }
    private Map<Long,CourseSummary> getSummaryMap(List<CourseSummary> list) {
        Map<Long,CourseSummary> map=new HashMap<>();
        for (CourseSummary courseSummary : list) {
            map.put(courseSummary.getId(),courseSummary);
        }
        return map;
    }
}
