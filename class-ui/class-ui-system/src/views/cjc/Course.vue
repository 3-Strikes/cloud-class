<template>
  <section>
    <!--工具条-->
    <el-col :span="24" class="toolbar" style="padding-bottom: 0px;">
      <el-form :model="filters" :inline="true">
        <el-form-item>
          <el-input v-model="filters.keywords" size="small" placeholder="关键字"></el-input>
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
    <el-table @row-click="rowClick" :data="courses" v-loading="listLoading" @selection-change="selsChange"
              highlight-current-row style="width: 100%;">
      <!--多选框-->
      <el-table-column type="selection" width="55e">
      </el-table-column>
      <!--其他都设置值,只有一个不设置值就自动适应了-->
      <el-table-column prop="name" label="课程名称">
      </el-table-column>
      <el-table-column prop="chapterCount" label="章节数">
      </el-table-column>
      <!--<el-table-column prop="courseType.name" label="类型">-->
      <!--</el-table-column>-->
      <el-table-column prop="gradeName" label="等级">
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
          <el-button size="small" @click="edit(scope.row)" icon="el-icon-edit" type="primary">编辑</el-button>
          <el-button type="danger" size="small" @click="del(scope.row)" icon="el-icon-remove">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <!--工具条-->
    <el-col :span="24" class="toolbar">
      <el-button type="danger" @click="batchRemove" :disabled="this.sels.length===0" icon="el-icon-remove" size="small">
        批量删除
      </el-button>
      <el-pagination layout="prev, pager, next" @current-change="handleCurrentChange" :page-size="10" :total="total"
                     style="float:right;">
      </el-pagination>
    </el-col>

    <!--新增界面-->
    <el-dialog title="新增" :visible.sync="addFormVisible" :close-on-click-modal="false" width="860px">
      <el-form :inline="true" :model="addForm" label-width="80px" ref="addForm">
        <el-form-item label="课程名称" prop="name">
          <el-input v-model="addForm.name" placeholder="课程名称" auto-complete="off" style="width: 300px"/>
        </el-form-item>
        <el-form-item label="适用人群" prop="forUser">
          <el-input v-model="addForm.forUser" placeholder="适用人群" auto-complete="off" style="width: 300px"/>
        </el-form-item>
        <el-form-item label="课程类型" prop="courseTypeId">
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
        <el-form-item label="课程周期">
          <el-date-picker style="width: 200px"
                          v-model="addForm.startTime"
                          type="date"
                          value-format="yyyy-MM-dd"
                          size="small"
                          placeholder="课程开始日期">
          </el-date-picker>
          -
          <el-date-picker style="width: 200px"
                          v-model="addForm.endTime"
                          type="date"
                          value-format="yyyy-MM-dd"
                          size="small"
                          placeholder="课程结束日期">
          </el-date-picker>
        </el-form-item>

        <el-form-item label="购买可看">
          <el-input placeholder="可看天数" type="number" v-model="addForm.validDays" auto-complete="off"
                    style="width: 165px"/>&nbsp;天
        </el-form-item>

        <el-form-item label="课程等级" prop="courseTypeId" style="width: 700px">
          <el-radio-group v-model="addForm.gradeId">
            <el-radio v-for="grade in grades" :label="grade.id">{{ grade.name }}</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item prop="logo" style="width: 400px">
          <!--<el-input type="text" v-model="employee.logo" auto-complete="off" placeholder="请输入logo！"></el-input>-->
          <el-upload
              class="upload-demo"
              :action="uploadUrl"
              name="fileName"
              :data="uploadPicData"
              list-type="picture"
              :on-success="handlePicSuccess"
              :limit="1"
              :before-upload="beforePicUpload"
              :on-remove="handlePicRemove"
          >
          <el-button size="small" type="primary" icon="el-icon-picture-outline">上传封面</el-button>
          &nbsp;&nbsp;<span slot="tip" class="el-upload__tip">支持500kb，格式jpg</span>
          </el-upload>
        </el-form-item>

        <el-form-item prop="logo">
          <!--<el-input type="text" v-model="employee.logo" auto-complete="off" placeholder="请输入logo！"></el-input>-->
          <el-upload
              class="upload-demo"
              :action="uploadUrl"
              :data="uploadZipData"
              name="fileName"
              :on-success="handleZipSuccess"
              :limit="1"
              :before-upload="beforeZipUpload"
              :on-remove="handleZipRemove"
          >
          <el-button size="small" type="primary" icon="el-icon-upload">上传课件</el-button>
          &nbsp;&nbsp;<span slot="tip" class="el-upload__tip">支持压缩格式</span>
          </el-upload>
        </el-form-item>
        <el-divider></el-divider>

        <el-form-item label="收费规则" prop="gradeId" size="width:100%">
          <el-radio-group v-model="addForm.chargeId">
            <el-radio @change="changeCharge" v-for="charge in charges" :label="charge.id">{{ charge.name }}</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="课程价格" prop="price">
          <el-input :disabled="priceDisabled" type="number" v-model="addForm.price" auto-complete="off"
                    style="width: 185px"/>
        </el-form-item>
        <el-form-item label="课程原价">
          <el-input :disabled="priceDisabled" type="number" v-model="addForm.priceOld" auto-complete="off"
                    style="width: 185px"/>
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
        <el-button type="primary" @click.native="addSubmit" icon="el-icon-check">提交</el-button>
      </div>
    </el-dialog>


    <el-dialog title="添加秒杀课程" :visible.sync="killCourseFormVisible" :close-on-click-modal="false">
      <el-form :model="killCourseForm" label-width="80px" ref="addForm">
        <el-form-item label="课程名字" prop="price">
          <el-input :disabled="true" v-model="killCourseForm.courseName" auto-complete="off"></el-input>
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
          <el-input v-model="killCourseForm.killPrice" auto-complete="off"></el-input>
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
        <el-button type="primary" @click.native="addKillCourseSubmit" icon="el-icon-check">提交</el-button>
      </div>
    </el-dialog>
  </section>
</template>

<script>
import {quillEditor} from "vue-quill-editor";
import "quill/dist/quill.core.css"
import "quill/dist/quill.snow.css"
import "quill/dist/quill.bubble.css"

export default {
  computed: {
    editor() {
      // 增加空值保护，避免编辑器未初始化报错
      return this.$refs.myQuillEditor.quill
    }
  },
  components: {
    quillEditor
  },
  data() {
    return {
      uploadUrl:this.$http.defaults.baseURL+"/common/oss/uploadFile",
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
      editorOption: {},
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
      killActivitys: [],
      addForm: {
        startTime: '',
        endTime: '',
        validDays: '',
        name: '',
        forUser: '',
        gradeId: '',
        teacharIds: [], // 修复：多选讲师默认值改为数组，避免传递字符串
        courseTypeId: [], // 修复：级联选择器默认值改为数组
        description: '',
        intro: '',
        chargeId: 1, // 修复：默认值设为1（免费），避免空值
        price: '',
        priceOld: '',
        qq: '',
        pic: '',
        zipResources: ''
      },
      listLoading: false,
      filters: {
        keywords: ''
      },
      page: 1,
      total: 0,
      courses: [],
      sels: [], // 修复：默认值改为数组
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
    handleRemove(file, fileList) {
      console.log(file)
      let files = [];
      fileList.forEach(function (file) {
        if (file.response && file.response.data && file.response.data) {
          let url = file.url = file.response.data;
          files.push(url);
        } else if (file.url) {
          files.push(file.url);
        }
      });
      if (file.response && file.response.data) {
        console.log("删除文件成功")
        var filePath = file.response.data;
        this.$http.delete("/common/oss/delete?url=" + filePath)
            .then(res => {
              if (res.code == 20000) {
                this.suc("删除成功");
              }
            })
      }
    },
    // 封面文件×号点击事件：判断并删除OSS文件
    async handlePicRemove(file, fileList) {
      if (file.response && file.response.data) {
        await this.handleRemove(file.response.data);
      }
      this.addForm.pic = "";
    },

    // 课件文件×号点击事件：判断并删除OSS文件
    async handleZipRemove(file, fileList) {
      if (file.response && file.response.data) {
        await this.handleRemove(file.response.data);
      }
      this.addForm.zipResources = "";
    },


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
      if (!this.row || this.row === "") {
        this.$message({message: '老铁，请选择数据', type: 'error'});
        return;
      }
      this.killCourseForm = {
        ...this.killCourseForm,
        killCount: "",
        killPrice: "",
        startTime: "",
        endTime: "",
        courseId: this.row.id,
        courseName: this.row.name,
        coursePic: this.row.pic,
        teacherNames: this.row.teacherNames
      };
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
        } else {
          this.$message({message: ajaxResult.message, type: 'error'});
        }
      }).catch(error => {
        this.$message({message: '保存失败!', type: 'error'});
      })
    },

    // 修复：重构getSign方法，纯async/await写法，避免then混用
    async getSign(data) {
      try {
        const response = await this.$http.get("/common/oss/sign");
        const resultObj = response.data.data;
        // 清空原有数据，避免参数污染
        Object.assign(data, {
          policy: resultObj.policy,
          signature: resultObj.signature,
          ossaccessKeyId: resultObj.accessid,
          dir: resultObj.dir,
          host: resultObj.host,
          key: `${resultObj.dir}/${this.getUUID()}_\${filename}`
        });
      } catch (error) {
        this.$message({message: "获取上传签名失败：" + error.message, type: "error"});
        // 抛出错误，让beforeUpload拦截上传
        throw error;
      }
    },

    //文件上传相关
    getUUID() {
      var s = [];
      var hexDigits = "0123456789abcdef";
      for (var i = 0; i < 36; i++) {
        s[i] = hexDigits.substr(Math.floor(Math.random() * 0x10), 1);
      }
      s[14] = "4";
      s[19] = hexDigits.substr((s[19] & 0x3) | 0x8, 1);
      s[8] = s[13] = s[18] = s[23] = "-";
      var uuid = s.join("");
      return uuid;
    },

    // 修复：beforePicUpload增加文件校验+纯async/await
    async beforePicUpload(file) {
      // 1. 校验文件是否存在
      if (!file) {
        this.$message({message: "请选择要上传的图片文件", type: "warning"});
        return false;
      }
      // 2. 校验文件大小（500kb）
      const isLt500K = file.size / 1024 < 500;
      if (!isLt500K) {
        this.$message({message: '封面图片大小不能超过500KB!', type: 'error'});
        return false;
      }
      // 3. 校验文件格式
      const isJPG = file.type === 'image/jpeg' || file.type === 'image/jpg';
      if (!isJPG) {
        this.$message({message: '封面图片仅支持jpg格式!', type: 'error'});
        return false;
      }
      // 4. 获取签名（确保签名获取完成再上传）
      await this.getSign(this.uploadPicData);
      return true;
    },

    // 修复：beforeZipUpload增加文件校验+纯async/await
    async beforeZipUpload(file) {
      // 1. 校验文件是否存在
      if (!file) {
        this.$message({message: "请选择要上传的课件文件", type: "warning"});
        return false;
      }
      // 2. 获取签名（确保签名获取完成再上传）
      await this.getSign(this.uploadZipData);
      return true;
    },

    handlePicSuccess(res, file) {
      this.addForm.pic = res.data;
      this.$message({message: '封面上传成功', type: 'success'});
    },

    handleZipSuccess(res, file) {
      this.addForm.zipResources = res.data;
      this.$message({message: '课件上传成功', type: 'success'});
    },

    addSubmit() {
      // 修复：判断级联选择器值是否为数组
      if (Array.isArray(this.addForm.courseTypeId) && this.addForm.courseTypeId.length > 0) {
        this.addForm.courseTypeId = this.addForm.courseTypeId[this.addForm.courseTypeId.length - 1];
      }

      var gradeName = "";
      for (var i = 0; i < this.grades.length; i++) {
        var grade = this.grades[i];
        if (grade.id === this.addForm.gradeId) {
          gradeName = grade.name;
          break;
        }
      }

      var param = {
        course: {
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
          type: 0
        },
        teacharIds: this.addForm.teacharIds
      };

      this.$http.post("/course/course/save", param).then(res => {
        var ajaxResult = res.data;
        if (ajaxResult.success) {
          this.$message({
            message: '保存成功!',
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
      }).catch(error => {
        this.$message({
          message: '提交异常：' + error.message,
          type: 'error'
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
      }).catch(error => {
        this.$message({message: "获取课程类型失败：" + error.message, type: "error"});
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

    onEditorReady(editor) {},

    addHandler() {
      this.addFormVisible = true;
    },

    handleCurrentChange(curentPage) {
      this.page = curentPage;
      this.getCourses();
    },

    getCourses() {
      let para = {
        "page": this.page,
        "keyword": this.filters.keywords
      };
      this.listLoading = true;
      this.$http.post("/course/course/pagelist", para).then(result => {
        this.total = result.data.data.total;
        this.courses = result.data.data.rows;
        this.listLoading = false;
      }).catch(error => {
        this.$message({message: error.message, type: 'error'});
        this.listLoading = false;
      });
    },

    onLineCourse() {
      if (!this.row || this.row.length === 0) {
        this.$message({message: '老铁，你不选中数据，臣妾上不了啊....', type: 'error'});
        return;
      }

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
      }).catch(error => {
        this.$message({message: error.message, type: 'error'});
      });
    },

    offLineCourse() {
      if (!this.row || this.row === "") {
        this.$message({message: '老铁，你不选中数据，臣妾下不了啊....', type: 'error'});
        return;
      }

      // 修复：判断row是否为数组（批量选择）
      const id = Array.isArray(this.row) ? this.row[0].id : this.row.id;
      this.$http.post("/course/course/offLineCourse/" + id).then(res => {
        var ajaxResult = res.data;
        if (ajaxResult.success) {
          this.$message({message: '老铁，下线成功.', type: 'success'});
          this.getCourses();
        } else {
          this.$message({message: ajaxResult.message, type: 'error'});
        }
      }).catch(error => {
        this.$message({message: error.message, type: 'error'});
      });
    },

    rowClick(row) {
      this.row = row;
    },

    statusFormatter: function (row, column) {
      return row.status == 1 ? '已上线' : '未上线';
    },

    getTeachers() {
      this.$http.get("/course/teacher/list")
          .then(result => {
            this.teachers = result.data.data;
          }).catch(error => {
        this.$message({message: error.message, type: 'error'});
      });
    },

    edit() {
      this.$message({message: "功能未开放", type: 'error'});
    },

    del() {
      this.$message({message: "功能未开放", type: 'error'});
    },

    batchRemove() {
      this.$message({message: "功能未开放", type: 'error'});
    },

    selsChange(sels) {
      this.row = sels;
      this.sels = sels;
    },
  },

  mounted() {
    this.getCourses();
    this.getCourseTypes();
    this.getTeachers();
    this.getKillActivitys();
  }
}
</script>

<style scoped>

</style>