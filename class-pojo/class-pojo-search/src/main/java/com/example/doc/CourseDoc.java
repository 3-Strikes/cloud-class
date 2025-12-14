package com.example.doc;

import com.example.constant.Constants;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.math.BigDecimal;
import java.util.Date;

@Document(indexName = Constants.COURSE_INDEX)
public class CourseDoc {
    @Id
    private Long id;//课程id
    @Field(type = FieldType.Text,analyzer = "ik_max_word",searchAnalyzer = "ik_max_word")
    private String name; //课程名称,分词
    @Field(type = FieldType.Text,analyzer = "ik_max_word",searchAnalyzer = "ik_max_word")
    private String forUser;//使用人群，分词
    @Field(type = FieldType.Long)
    private Long courseTypeId;//课程分类

    @Field(type = FieldType.Keyword)
    private String gradeName;//等级名

    @Field(type = FieldType.Long)
    private Long gradeId;//登记id

    @Field(type = FieldType.Date)
    private Date startTime;//课程开始时间
    @Field(type = FieldType.Date)
    private Date endTime;//结束时间
    @Field(type = FieldType.Keyword)
    private String pic;//封面
    @Field(type = FieldType.Date)
    private Date onlineTime;//上架时间
    @Field(type = FieldType.Text,analyzer = "ik_max_word",searchAnalyzer = "ik_max_word")
    private String teacherNames;//讲师名   a,b，分词
    @Field(type = FieldType.Integer)
    private Integer charge;//收费规则：，收费1免费，2收费
    @Field(type = FieldType.Double)
    private Double price;//现价
    @Field(type = FieldType.Double)
    private Double priceOld;//原价
    @Field(type = FieldType.Integer)
    private Integer saleCount;//销量

    @Field(type = FieldType.Integer)
    private Integer viewCount;//浏览量
    @Field(type = FieldType.Integer)
    private Integer commentCount;//评论数

    @Override
    public String toString() {
        return "CourseDoc{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", forUser='" + forUser + '\'' +
                ", courseTypeId=" + courseTypeId +
                ", gradeName='" + gradeName + '\'' +
                ", gradeId=" + gradeId +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", pic='" + pic + '\'' +
                ", onlineTime=" + onlineTime +
                ", teacherNames='" + teacherNames + '\'' +
                ", charge=" + charge +
                ", price=" + price +
                ", priceOld=" + priceOld +
                ", saleCount=" + saleCount +
                ", viewCount=" + viewCount +
                ", commentCount=" + commentCount +
                '}';
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getForUser() {
        return forUser;
    }

    public void setForUser(String forUser) {
        this.forUser = forUser;
    }

    public Long getCourseTypeId() {
        return courseTypeId;
    }

    public void setCourseTypeId(Long courseTypeId) {
        this.courseTypeId = courseTypeId;
    }

    public String getGradeName() {
        return gradeName;
    }

    public void setGradeName(String gradeName) {
        this.gradeName = gradeName;
    }

    public Long getGradeId() {
        return gradeId;
    }

    public void setGradeId(Long gradeId) {
        this.gradeId = gradeId;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public String getPic() {
        return pic;
    }

    public void setPic(String pic) {
        this.pic = pic;
    }

    public Date getOnlineTime() {
        return onlineTime;
    }

    public void setOnlineTime(Date onlineTime) {
        this.onlineTime = onlineTime;
    }

    public String getTeacherNames() {
        return teacherNames;
    }

    public void setTeacherNames(String teacherNames) {
        this.teacherNames = teacherNames;
    }

    public Integer getCharge() {
        return charge;
    }

    public void setCharge(Integer charge) {
        this.charge = charge;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Double getPriceOld() {
        return priceOld;
    }

    public void setPriceOld(Double priceOld) {
        this.priceOld = priceOld;
    }

    public Integer getSaleCount() {
        return saleCount;
    }

    public void setSaleCount(Integer saleCount) {
        this.saleCount = saleCount;
    }

    public Integer getViewCount() {
        return viewCount;
    }

    public void setViewCount(Integer viewCount) {
        this.viewCount = viewCount;
    }

    public Integer getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(Integer commentCount) {
        this.commentCount = commentCount;
    }
}
