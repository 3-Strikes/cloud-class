<template>
  <section>
    <!--工具条-->
    <el-col :span="24" class="toolbar" style="padding-bottom: 0px;">
      <el-form :model="filters" :inline="true">
        <el-form-item>
          <el-input v-model="filters.keywords" size="small"  placeholder="关键字" ></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="warning" v-on:click="getCourses" size="small" icon="el-icon-search">查询课程</el-button>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="addHandler" size="small" icon="el-icon-notebook-1">新增课程</el-button>
        </el-form-item>
        <el-form-item>
          <el-button type="success" @click="onLineCourse" size="small" icon="el-icon-s-promotion">课程发布</el-button>
        </el-form-item>
        <el-form-item>
          <el-button type="danger" @click="offLineCourse" size="small" icon="el-icon-download">课程下架</el-button>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="killCourseModelView" size="small" icon="el-icon-sell">加入秒杀</el-button>
        </el-form-item>
      </el-form>
    </el-col>

    <!--列表v-loading="listLoading"-->
    <el-table @row-click="rowClick" :data="courses" v-loading="listLoading"  @selection-change="selsChange"
              highlight-current-row  style="width: 100%;">
      <!--多选框-->
      <el-table-column type="selection" width="55">
      </el-table-column>
      <!--其他都设置值,只有一个不设置值就自动适应了-->
      <el-table-column prop="name" label="课程名称" >
      </el-table-column>
      <el-table-column prop="chapterCount" label="章节数" >
      </el-table-column>
      <!--<el-table-column prop="courseType.name" label="类型">-->
      <!--</el-table-column>-->
      <el-table-column prop="gradeName" label="等级" >
      </el-table-column>
      <el-table-column prop="status" label="状态" :formatter="statusFormatter">
      </el-table-column>
      <el-table-column prop="forUser" label="适用人群" width="220">
      </el-table-column>
      <!--<el-table-column prop="tenantName" label="所属机构">-->
      <!--</el-table-column>-->
      <el-table-column prop="teacherNames" label="讲师" width="140">
      </el-table-column>
      <el-table-column label="操作" width="200">
        <template scope="scope">
          <el-button size="small"  @click="edit(scope.row)" icon="el-icon-edit" type="primary">编辑</el-button>
          <el-button type="danger" size="small" @click="del(scope.row)" icon="el-icon-remove">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <!--工具条-->
    <el-col :span="24" class="toolbar">
      <el-button type="danger"  @click="batchRemove" :disabled="this.sels.length===0" icon="el-icon-remove" size="small">批量删除</el-button>
      <el-pagination layout="prev, pager, next" @current-change="handleCurrentChange"  :page-size="10" :total="total" style="float:right;">
      </el-pagination>
    </el-col>

    <!--新增/编辑界面（复用）-->
    <el-dialog :title="isEdit ? '编辑课程' : '新增课程'" :visible.sync="addFormVisible"  :close-on-click-modal="false" width="860px">
      <el-form :inline="true" :model="addForm" label-width="80px"  ref="addForm" :rules="formRules">
        <el-form-item label="课程名称" prop="name" >
          <el-input v-model="addForm.name" placeholder="课程名称" auto-complete="off" style="width: 300px" />
        </el-form-item>
        <el-form-item label="适用人群" prop="forUser">
          <el-input v-model="addForm.forUser" placeholder="适用人群" auto-complete="off" style="width: 300px"/>
        </el-form-item>
        <el-form-item label="课程类型" prop="courseTypeId" >
          <el-cascader style="width: 300px"
                       :props="courseTypeProps"
                       v-model="addForm.courseTypeId"
                       placeholder="课程类型"
                       :options="courseTypes"
                       expand-trigger="hover"
                       :show-all-levels="false"
                       filterable
                       change-on-select
          ></el-cascader>
        </el-form-item>
        <el-form-item label="添加讲师" prop="teachers">
          <el-select v-model="addForm.teacharIds" multiple placeholder="可选多个讲师" style="width: 300px">
            <el-option
                v-for="item in teachers"
                :key="item.id"
                :label="item.name"
                :value="item.id">
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="课程周期" >
          <el-date-picker  style="width: 200px"
                           v-model="addForm.startTime"
                           type="date"
                           value-format="yyyy-MM-dd"
                           size="small"
                           placeholder="课程开始日期">
          </el-date-picker> -
          <el-date-picker   style="width: 200px"
                            v-model="addForm.endTime"
                            type="date"
                            value-format="yyyy-MM-dd"
                            size="small"
                            placeholder="课程结束日期">
          </el-date-picker>
        </el-form-item >

        <el-form-item label="购买可看" >
          <el-input placeholder="可看天数"  type="number" v-model="addForm.validDays" auto-complete="off" style="width: 165px"/>&nbsp;天
        </el-form-item>

        <el-form-item label="课程等级" prop="gradeId"  style="width: 700px">
          <el-radio-group v-model="addForm.gradeId">
            <el-radio v-for="grade in grades" :label="grade.id">{{grade.name}}</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item prop="logo" style="width: 400px" >
          <!--<el-input type="text" v-model="employee.logo" auto-complete="off" placeholder="请输入logo！"></el-input>-->
          <el-upload
              class="upload-demo"
              action="http://localhost:10010/ymcc/common/oss/uploadFile"
              name="fileName"
              :data="uploadPicData"
              list-type="picture"
              :on-success="handlePicSuccess"
              :limit="1">
            <el-button size="small" type="primary"  icon="el-icon-picture-outline">上传封面</el-button>
            &nbsp;&nbsp;<span slot="tip" class="el-upload__tip">支持500kb，格式jpg</span>
          </el-upload>
        </el-form-item>

        <el-form-item prop="logo"  >
          <!--<el-input type="text" v-model="employee.logo" auto-complete="off" placeholder="请输入logo！"></el-input>-->
          <el-upload
              class="upload-demo"
              action="http://localhost:10010/ymcc/common/oss/uploadFile"
              :data="uploadZipData"
              name="fileName"
              :on-success="handleZipSuccess"
              :limit="1">
            <el-button size="small" type="primary" icon="el-icon-upload">上传课件</el-button>
            &nbsp;&nbsp;<span slot="tip" class="el-upload__tip">支持压缩格式</span>
          </el-upload>
        </el-form-item>
        <el-divider></el-divider>

        <el-form-item label="收费规则" prop="chargeId" size="width:100%">
          <el-radio-group v-model="addForm.chargeId">
            <el-radio @change="changeCharge" v-for="charge in charges" :label="charge.id">{{charge.name}}</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="课程价格" prop="price">
          <el-input :disabled="priceDisabled" type="number" v-model="addForm.price" auto-complete="off" style="width: 185px"/>
        </el-form-item>
        <el-form-item label="课程原价">
          <el-input :disabled="priceDisabled" type="number" v-model="addForm.priceOld" auto-complete="off" style="width: 185px"/>
        </el-form-item>

        <el-form-item label="咨询QQ" prop="qq">
          <el-input v-model="addForm.qq" auto-complete="off" style="width: 150px"></el-input>
        </el-form-item>


        <el-form-item label="课程简介" prop="description">
          <el-input style="width: 450px"
                    :rows="2"
                    placeholder="请输入内容"
                    v-model="addForm.description">
          </el-input>
        </el-form-item>

        <el-form-item label="课程详情" prop="intro">
          <div class="edit_container">
            <quill-editor
                v-model="addForm.intro"
                ref="myQuillEditor"
                class="editer"
                :options="editorOption"
                @ready="onEditorReady($event)">
            </quill-editor>
          </div>
        </el-form-item>


      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click.native="addFormVisible = false" icon="el-icon-remove">取消</el-button>
        <el-button type="primary" @click.native="addSubmit" icon="el-icon-check" >提交</el-button>
      </div>
    </el-dialog>



    <el-dialog title="添加秒杀课程" :visible.sync="killCourseFormVisible"  :close-on-click-modal="false">
      <el-form :model="killCourseForm" label-width="80px"  ref="addForm">
        <el-form-item label="课程名字" prop="price">
          <el-input :disabled="true"  v-model="killCourseForm.courseName" auto-complete="off"></el-input>
        </el-form-item>

        <el-form-item label="秒杀活动" prop="activityId">
          <el-select v-model="killCourseForm.activityId" placeholder="请选择秒杀活动">
            <el-option v-for="item in killActivitys"
                       :key="item.id"
                       :label="item.name"
                       :value="item.id">
              <span style="float: left">{{ item.name }}</span>
              <span style="float: right; color: #8492a6; font-size: 13px">{{ item.timeStr }}</span>
            </el-option>
          </el-select>
          &nbsp;&nbsp;秒杀课程加入秒杀活动，秒杀时间以活动时间为准
        </el-form-item>

        <el-form-item label="秒杀价格" prop="price">
          <el-input  v-model="killCourseForm.killPrice" auto-complete="off"></el-input>
        </el-form-item>

        <el-form-item label="秒杀数量" prop="name">
          <el-input v-model="killCourseForm.killCount" auto-complete="off"></el-input>
        </el-form-item>
        <!--
        <el-form-item label="时间范围" >
          <el-date-picker
              v-model="killCourseForm.startTime"
              type="datetime"
              value-format="yyyy-MM-dd HH:mm:ss"
              size="small"
              placeholder="秒杀开始日期">
          </el-date-picker>

          <el-date-picker
              v-model="killCourseForm.endTime"
              type="datetime"
              value-format="yyyy-MM-dd HH:mm:ss"
              size="small"
              placeholder="秒杀结束日期">
          </el-date-picker>
        </el-form-item>
        -->
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click.native="killCourseFormVisible = false" icon="el-icon-remove">取消</el-button>
        <el-button type="primary" @click.native="addKillCourseSubmit"  icon="el-icon-check">提交</el-button>
      </div>
    </el-dialog>
  </section>
</template>

<script>
import {quillEditor} from "vue-quill-editor"; //调用编辑器
import "quill/dist/quill.core.css"
import "quill/dist/quill.snow.css"
import "quill/dist/quill.bubble.css"

export default {
  computed: {
    editor() {
      return this.$refs.myQuillEditor.quill
    }
  },
  components: {//使用编辑器
    quillEditor
  },
  data() {
    return {
      uploadPicData: {},
      uploadZipData: {},
      uploadResource: {
        courseId: 1,
        paths: []
      },
      row: "",
      courseTypeProps: {
        value: "id",
        label: "name"
      },
      priceDisabled: true,
      editorOption: {},//富文本编辑框配置
      grades: [
        {id: 1, name: "青铜"}, {id: 2, name: "白银"}, {id: 3, name: "黄金"}, {id: 4, name: "白金"}, {
          id: 5,
          name: "钻石"
        }
      ],
      charges: [
        {"id": 1, "name": "免费"},
        {"id": 2, "name": "收费"}
      ],
      teachers: [],
      courseTypes: [],
      addFormVisible: false,
      isEdit: false, // 新增：区分新增/编辑模式
      //表单验证规则
      formRules: {
        name: [{required: true, message: '课程名称不能为空', trigger: 'blur'}],
        forUser: [{required: true, message: '适用人群不能为空', trigger: 'blur'}],
        gradeId: [{required: true, message: '请选择课程等级', trigger: 'change'}],
        courseTypeId: [{required: true, message: '请选择课程类型', trigger: 'change'}]
      },
      //images:[xxx.jgp,xxxx,jpg,xxxx.jpg],
      killActivitys: [],
      addForm: {
        id: '', // 新增：课程ID（编辑时用）
        startTime: '',
        endTime: '',
        validDays: '',
        name: '',
        forUser: '',
        gradeId: '',
        teacharIds: [], // 修正：原拼写错误 teacharIds -> 改为数组类型
        courseTypeId: [], // 级联选择器值为数组
        description: '',
        intro: '',
        chargeId: '',
        price: '',
        priceOld: '',
        qq: '',
        pic: '',
        zipResources: ''
      },
      listLoading: false,
      //查询对象
      filters: {
        keywords: ''
      },
      page: 1,//当前页,要传递到后台的
      total: 0, //分页总数
      courses: [], //当前页数据
      sels: [], // 修正：初始化为数组，而非字符串
      //秒杀相关================
      killCourseFormVisible: false,
      killCourseForm: {
        courseId: "",
        killCount: "",
        startTime: "",
        endTime: "",
        killPrice: "",
        courseName: "",
        coursePic: "",
        teacherNames: "",
        activityId: "",
      },
    }
  },
  methods: {
    //秒杀相关
    getKillActivitys() {
      this.$http.get("/kill/killActivity/list").then(result => {
        let {data, success, message} = result.data;
        if (success) {
          this.killActivitys = data;
        } else {
          this.$message({message: message, type: 'error'});
        }
      }).catch(error => {
        this.$message({message: error.message, type: 'error'});
      });
    },
    killCourseModelView() {
      //获取选中的行
      if (!this.row || this.row === "") {
        this.$message({message: '老铁，请选择数据', type: 'error'});
        return;
      }
      this.killCourseForm.killCount = "";
      this.killCourseForm.killPrice = "";
      this.killCourseForm.startTime = "";
      this.killCourseForm.endTime = "";

      this.killCourseForm.courseId = this.row.id;
      this.killCourseForm.courseName = this.row.name;
      this.killCourseForm.coursePic = this.row.pic;
      this.killCourseForm.teacherNames = this.row.teacherNames;
      this.killCourseFormVisible = true;
    },
    addKillCourseSubmit() {
      this.$http.post("/kill/killCourse/save", this.killCourseForm).then(res => {
        var ajaxResult = res.data;
        if (ajaxResult.success) {
          this.$message({
            message: '加入秒杀成功，请在秒杀中心查看!',
            type: 'success'
          });
          this.killCourseFormVisible = false;
          //this.getCourses();
        } else {
          this.$message({message: ajaxResult.message, type: 'error'});
        }
      }).catch(error => {
        this.$message({message: '保存失败!', type: 'error'});
      })
    },
    async getSign(data) {
      //让这个请求变成同步请求
      await this.$http.get("/common/oss/sign").then(response => {
        //设置相关的参数
        var resultObj = response.data.data;
        data.policy = resultObj.policy;
        data.signature = resultObj.signature;
        data.ossaccessKeyId = resultObj.accessid;
        data.dir = resultObj.dir;
        data.host = resultObj.host;
        //上传的文件名，使用UUID处理一下
        data.key = resultObj.dir + '/' + this.getUUID() + '_${filename}';
      });
    },
    //文件上传===============================================================
    getUUID() {
      var s = [];
      var hexDigits = "0123456789abcdef";
      for (var i = 0; i < 36; i++) {
        s[i] = hexDigits.substr(Math.floor(Math.random() * 0x10), 1);
      }
      s[14] = "4"; // bits 12-15 of the time_hi_and_version field to 0010
      s[19] = hexDigits.substr((s[19] & 0x3) | 0x8, 1); // bits 6-7 of the clock_seq_hi_and_reserved to 01
      s[8] = s[13] = s[18] = s[23] = "-";
      var uuid = s.join("");
      return uuid;
    },
    async beforePicUpload() {
      await this.getSign(this.uploadPicData);
    },
    async beforeZipUpload() {
      await this.getSign(this.uploadZipData);
    },
    handlePicSuccess(res, file) {
      //console.debug(res,file);
      this.addForm.pic = res.data;
      this.$message({message: '上传成功', type: 'success'});
    },
    handleZipSuccess(res, file) {
      //上传的完整的文件地址
      this.addForm.zipResources = res.data;
      this.$message({message: '上传成功', type: 'success'});
    },
    addSubmit() {
      // 表单验证
      this.$refs.addForm.validate((valid) => {
        if (!valid) return;

        this.addForm.courseTypeId = this.addForm.courseTypeId[this.addForm.courseTypeId.length - 1];
        var gradeName;
        for (var i = 0; i < this.grades.length; i++) {
          var grade = this.grades[i];
          if (grade.id === this.addForm.gradeId) {
            gradeName = grade.name;
            break;
          }
        }

        var param = {
          course: {
            id: this.addForm.id, // 新增：编辑时传递ID
            courseTypeId: this.addForm.courseTypeId,
            name: this.addForm.name,
            forUser: this.addForm.forUser,
            gradeId: this.addForm.gradeId,
            gradeName: gradeName,
            pic: this.addForm.pic,
            startTime: this.addForm.startTime,
            endTime: this.addForm.endTime
          },
          courseDetail: {
            description: this.addForm.description,
            intro: this.addForm.intro
          },
          courseMarket: {
            charge: this.addForm.chargeId,
            qq: this.addForm.qq,
            price: this.addForm.price,
            priceOld: this.addForm.priceOld,
            validDays: this.addForm.validDays
          },
          courseResource: {
            resources: this.addForm.zipResources,
            type: 0	//课件
          },
          teacharIds: this.addForm.teacharIds
        };

        this.$http.post("/course/course/save", param).then(res => {
          var ajaxResult = res.data;
          if (ajaxResult.success) {
            this.$message({
              message: this.isEdit ? '编辑成功!' : '保存成功!',
              type: 'success'
            });
            this.addFormVisible = false;
            this.getCourses();
          } else {
            this.$message({
              message: '提交失败[' + res.data.message + "]",
              type: 'error'
            });
          }
        });
      });
    },
    getGrades() {
      this.$http.get("/system/systemdictionaryitem/listBySn/dj").then(result => {
        this.grades = result.data.data;
      });
    },
    getCourseTypes() {
      this.$http.get("/course/courseType/treeData").then(result => {
        this.courseTypes = result.data.data;
      });
    },
    changeCharge(chargeId) {
      if (chargeId === 1) {
        this.priceDisabled = true;
        this.addForm.price = "";
        this.addForm.priceOld = "";
      } else {
        this.priceDisabled = false;
      }
    },
    onEditorReady(editor) {
      //当富文本编辑框初始化好执行
    },
    addHandler() {
      this.isEdit = false; // 新增模式
      // 重置表单
      this.addForm = {
        id: '',
        startTime: '',
        endTime: '',
        validDays: '',
        name: '',
        forUser: '',
        gradeId: '',
        teacharIds: [],
        courseTypeId: [],
        description: '',
        intro: '',
        chargeId: '',
        price: '',
        priceOld: '',
        qq: '',
        pic: '',
        zipResources: ''
      };
      // 重置表单验证
      this.$nextTick(() => {
        this.$refs.addForm.resetFields();
      });
      this.addFormVisible = true;
    },
    handleCurrentChange(curentPage) {
      this.page = curentPage;
      this.getCourses();
    },
    getCourses() {
      //发送Ajax请求后台获取数据  axios
      //添加分页条件及高级查询条件
      let para = {
        "page": this.page,
        "rows": 10, // 补充：后端需要rows参数
        "keyword": this.filters.keywords
      };
      this.listLoading = true; //显示加载圈
      //分页查询
      this.$http.post("/course/course/pagelist", para).then(result => {
        this.total = result.data.data.total;
        this.courses = result.data.data.rows;
        this.listLoading = false;  //关闭加载圈
      }).catch(error => {
        this.$message({message: error.message, type: 'error'});
        this.listLoading = false;
      });
    },
    onLineCourse() {
      //获取选中的行
      if (!this.row || this.row.length === 0) {
        this.$message({message: '老铁，你不选中数据，臣妾上不了啊....', type: 'error'});
        return;
      }

      // 获取this.row中的所有id
      var arrId = [];
      for (var i = 0; i < this.row.length; i++) {
        arrId.push(this.row[i].id);
      }
      this.$http.post("/course/course/onLineCourse", arrId).then(res => {
        var ajaxResult = res.data;
        if (ajaxResult.success) {
          this.$message({message: '老铁，上线成功.', type: 'success'});
          this.getCourses();
        } else {
          this.$message({message: ajaxResult.message, type: 'error'});
        }
        // 清空
        arrId.length = 0;
      }).catch(error => {
        this.$message({message: error.message, type: 'error'});
      });

    },
    offLineCourse() {
      //获取选中的行
      if (!this.row || (typeof this.row === 'object' && this.row.length === 0)) {
        this.$message({message: '老铁，你不选中数据，臣妾下不了啊....', type: 'error'});
        return;
      }
      // 兼容单行/多行选中
      const ids = Array.isArray(this.row) ? this.row.map(item => item.id) : [this.row.id];

      this.$http.post("/course/course/offLineCourse/" + ids[0]).then(res => {
        var ajaxResult = res.data;
        if (ajaxResult.success) {
          this.$message({message: '老铁，下线成功.', type: 'success'});
          this.getCourses();
        } else {
          this.$message({message: ajaxResult.message, type: 'error'});
        }
      }).catch(error => {
        this.$message({message: error.message, type: 'error'});
      })
    },
    rowClick(row) {
      console.debug(row, "===================");
      this.row = row;
    },
    statusFormatter: function (row, column) {
      return row.status == 1 ? '已上线' : '未上线';
    },

    //讲师
    getTeachers() {
      this.$http.get("/course/teacher/list")
          .then(result => {
            this.teachers = result.data.data;
            console.log(this.teachers)
          }).catch(error => {
        this.$message({message: error.message, type: 'error'});
      });
    },

    // ========== 新增：编辑课程功能 ==========
    edit(row) {
      if (!row || !row.id) {
        this.$message.warning('请选择有效的课程进行编辑！');
        return;
      }
      this.isEdit = true; // 标记为编辑模式
      this.listLoading = true;
      // 获取课程详情
      this.$http.get(`/course/course/${row.id}`)
          .then(res => {
            if (res.data.success) {
              const course = res.data.data;
              // 回显表单数据
              this.addForm = {
                id: course.id,
                name: course.name || '',
                forUser: course.forUser || '',
                gradeId: course.gradeId || '',
                gradeName: course.gradeName || '',
                courseTypeId: course.courseTypeId ? [course.courseTypeId] : [], // 级联选择器适配
                teacharIds: course.teacharIds || [],
                startTime: course.startTime || '',
                endTime: course.endTime || '',
                validDays: course.validDays || '',
                chargeId: course.charge || '',
                price: course.price || '',
                priceOld: course.priceOld || '',
                qq: course.qq || '',
                pic: course.pic || '',
                zipResources: course.zipResources || '',
                description: course.description || '',
                intro: course.intro || ''
              };
              // 处理收费规则禁用状态
              this.priceDisabled = this.addForm.chargeId === 1;
              // 打开弹窗
              this.addFormVisible = true;
            } else {
              this.$message.error('获取课程详情失败：' + res.data.message);
            }
            this.listLoading = false;
          })
          .catch(error => {
            this.$message.error('请求异常：' + error.message);
            this.listLoading = false;
          });
    },

    // ========== 新增：单个删除功能 ==========
    del(row) {
      if (!row || !row.id) {
        this.$message.warning('请选择有效的课程进行删除！');
        return;
      }
      this.$confirm(`确定要删除课程【${row.name}】吗？删除后不可恢复！`, '警告', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.listLoading = true;
        // 调用后端删除接口
        this.$http.delete(`/course/course/${row.id}`)
            .then(res => {
              if (res.data.success) {
                this.$message.success('删除成功！');
                this.getCourses(); // 刷新列表
              } else {
                this.$message.error('删除失败：' + res.data.message);
              }
              this.listLoading = false;
            })
            .catch(error => {
              this.$message.error('删除异常：' + error.message);
              this.listLoading = false;
            });
      }).catch(() => {
        this.$message.info('已取消删除');
      });
    },

    // ========== 新增：批量删除功能 ==========
    batchRemove() {
      if (this.sels.length === 0) {
        this.$message.warning('请选择要删除的课程！');
        return;
      }
      const courseNames = this.sels.map(item => item.name).join('、');
      this.$confirm(`确定要批量删除选中的【${courseNames}】等${this.sels.length}门课程吗？删除后不可恢复！`, '警告', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.listLoading = true;
        // 方式1：循环调用单个删除接口（后端无批量接口时）
        const deletePromises = this.sels.map(item =>
            this.$http.delete(`/course/course/${item.id}`)
        );
        // 等待所有删除请求完成
        Promise.all(deletePromises)
            .then(results => {
              const failCount = results.filter(res => !res.data.success).length;
              if (failCount === 0) {
                this.$message.success(`批量删除成功！共删除${this.sels.length}门课程`);
              } else {
                this.$message.warning(`部分课程删除失败，成功删除${this.sels.length - failCount}门`);
              }
              this.getCourses(); // 刷新列表
              this.sels = []; // 清空选中
              this.listLoading = false;
            })
            .catch(error => {
              this.$message.error('批量删除异常：' + error.message);
              this.listLoading = false;
            });

        // 方式2：如果后端新增批量删除接口，可替换为：
        // const ids = this.sels.map(item => item.id).join(',');
        // this.$http.delete(`/course/course/batch/${ids}`)
        // 	.then(res => { ... });
      }).catch(() => {
        this.$message.info('已取消批量删除');
      });
    },

    // 选中行变化
    selsChange(sels) {
      console.debug(sels);
      this.row = sels; // 批量操作时用
      this.sels = sels;
    },
  },

  mounted() {
    this.getCourses();
    //this.getGrades();
    this.getCourseTypes();
    this.getTeachers();
    this.getKillActivitys();
  }
}

</script>

<style scoped>
.edit_container {
  min-height: 200px;
}
</style>