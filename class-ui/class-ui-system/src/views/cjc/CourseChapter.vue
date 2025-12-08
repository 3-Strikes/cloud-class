<template>
  <section>
    <!--工具条-->
    <el-col :span="24" class="toolbar" style="padding-bottom: 0px;">
      <el-form :model="filters" :inline="true">
        <el-form-item>
          <el-input v-model="filters.keyword" size="small" placeholder="课程名/章节名"></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="warning" v-on:click="getCoursesChapter" size="small" icon="el-icon-search">章节查询</el-button>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" size="small" @click="addHandler" icon="el-icon-plus">新增章节</el-button>
        </el-form-item>
      </el-form>
    </el-col>

    <!--列表-->
    <el-table :data="coursesChapter" v-loading="listLoading" @row-click="chapterRowSlect" @selection-change="selsChange"
              highlight-current-row style="width: 100%;">
      <el-table-column type="selection" width="55"></el-table-column>
      <el-table-column prop="courseName" label="课程名称"></el-table-column>
      <el-table-column prop="name" label="章节名称"></el-table-column>
      <el-table-column prop="number" label="章节号"></el-table-column>
      <el-table-column label="操作" width="200">
        <template scope="scope">
          <el-button size="small" @click="edit(scope.row)" icon="el-icon-edit" type="primary">编辑</el-button>
          <el-button type="danger" size="small" @click="del(scope.row)" icon="el-icon-remove">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!--工具条-->
    <el-col :span="24" class="toolbar">
      <el-button type="danger" @click="batchRemove" :disabled="this.sels.length===0" icon="el-icon-remove" size="small">批量删除</el-button>
      <el-pagination layout="prev, pager, next" @current-change="handleCurrentChange" :page-size="10" :total="total" style="float:right;"></el-pagination>
    </el-col>

    <!--新增/编辑章节弹窗-->
    <el-dialog title="章节管理" :visible.sync="addFormVisible" :close-on-click-modal="false" width="800px">
      <el-form :model="addForm" label-width="80px" ref="addForm" :rules="rules">
        <el-form-item label="章节名称" prop="name">
          <el-input v-model="addForm.name" auto-complete="off"></el-input>
        </el-form-item>
        <el-form-item label="所属课程" prop="courseId">
          <el-input v-model="addForm.courseName" auto-complete="off" style="width: 550px" disabled></el-input>
          <el-button type="primary" @click="dialogTableVisible = true" icon="el-icon-search">选择课程</el-button>
        </el-form-item>
        <el-form-item label="章节号" prop="number" v-if="isEdit">
          <el-input v-model="addForm.number" min="1" type="number" auto-complete="off"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click.native="addFormVisible = false" icon="el-icon-remove">取消</el-button>
        <el-button type="primary" @click.native="saveCourseChapter" icon="el-icon-check">提交</el-button>
      </div>
    </el-dialog>

    <!--选择课程弹窗-->
    <el-dialog title="选择课程" :visible.sync="dialogTableVisible" :close-on-click-modal="false" width="800px">
      <el-form :model="courseAddForm" label-width="80px" ref="dialogTableAddForm">
        <el-form-item label-width="0px" style="border-bottom: 1px solid #eeeeee;padding-bottom: 20px">
          <el-input v-model="courseAddForm.keyword" placeholder="搜索课程名" style="width: 200px" size="small"></el-input>
          <el-button type="warning" v-on:click="getCourses" icon="el-icon-search" size="small">查询</el-button>
          &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span style="color: red">搜索课程后加载数据，双击行进行选择</span>
        </el-form-item>

        <el-form-item label-width="0px">
          <el-table :data="courses" highlight-current-row @row-dblclick="selectCourse">
            <el-table-column property="id" label="ID" width="100"></el-table-column>
            <el-table-column property="name" label="课程"></el-table-column>
            <el-table-column property="gradeName" label="等级"></el-table-column>
          </el-table>
        </el-form-item>

        <el-form-item label-width="0px">
          <el-pagination layout="prev, pager, next" :page-size="10" :total="courseTotal" style="float:right;"></el-pagination>
        </el-form-item>
      </el-form>
    </el-dialog>
  </section>
</template>

<script>
export default {
  data() {
    return {
      // 标记是否为编辑状态
      isEdit: false,
      // 章节行数据
      chapterRow: "",
      // 课程列表
      courses: [],
      courseTotal: 0,
      // 弹窗控制
      addFormVisible: false,
      dialogTableVisible: false,
      // 课程搜索表单
      courseAddForm: {
        keyword: '',
        page: 1,
        rows: 10
      },
      // 章节表单
      addForm: {
        id: "",
        name: '',
        courseId: "",
        courseName: '',
        number: ''
      },
      // 表单校验规则
      rules: {
        name: [
          { required: true, message: '章节名称不能为空', trigger: 'blur' }
        ],
        courseId: [
          { required: true, message: '请选择所属课程', trigger: 'change' }
        ]
      },
      // 加载状态
      listLoading: false,
      // 查询条件
      filters: {
        keyword: ''
      },
      // 分页参数
      page: 1,
      total: 0,
      // 章节列表
      coursesChapter: [],
      // 选中的章节
      sels: []
    }
  },
  methods: {
    // 保存/编辑章节
    saveCourseChapter() {
      this.$refs.addForm.validate((valid) => {
        if (valid) {
          // 提交数据
          this.$http.post("/course/courseChapter/save", this.addForm).then(res => {
            const { success, message } = res.data;
            if (success) {
              this.$message({ message: this.isEdit ? "编辑成功" : "新增成功", type: 'success' });
              this.addFormVisible = false;
              this.getCoursesChapter();
              // 重置表单
              this.resetForm();
            } else {
              this.$message({ message: "操作失败[" + message + "]", type: 'error' });
            }
          }).catch(error => {
            this.$message({ message: "操作失败[" + error.message + "]", type: 'error' });
          });
        }
      });
    },

    // 选择课程
    selectCourse(row) {
      this.courseAddForm.keyword = "";
      this.addForm.courseId = row.id;
      this.addForm.courseName = row.name;

      // 新增时自动获取最大章节号+1
      if (!this.isEdit) {
        this.$http.get(`/course/courseChapter/maxNumber/${row.id}`).then(res => {
          this.addForm.number = res.data.data + 1;
        });
      }

      this.dialogTableVisible = false;
    },

    // 获取课程列表
    getCourses() {
      this.$http.post("/course/course/pagelist", this.courseAddForm).then(result => {
        this.courses = result.data.data.rows;
        this.courseTotal = result.data.data.total;
      });
    },

    // 新增章节
    addHandler() {
      this.isEdit = false;
      this.resetForm();
      this.addFormVisible = true;
    },

    // 编辑章节
    edit(row) {
      this.isEdit = true;
      // 回显数据
      this.addForm = {
        id: row.id,
        name: row.name,
        courseId: row.courseId,
        courseName: row.courseName,
        number: row.number
      };
      this.addFormVisible = true;
    },

    // 删除单个章节
    del(row) {
      this.$confirm('确定删除该章节吗?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.$http.delete(`/course/courseChapter/${row.id}`).then(res => {
          const { success, message } = res.data;
          if (success) {
            this.$message({ message: '删除成功', type: 'success' });
            this.getCoursesChapter();
          } else {
            this.$message({ message: "删除失败[" + message + "]", type: 'error' });
          }
        }).catch(error => {
          this.$message({ message: "删除失败[" + error.message + "]", type: 'error' });
        });
      }).catch(() => {
        this.$message({ message: '已取消删除', type: 'info' });
      });
    },

    // 批量删除
    batchRemove() {
      this.$confirm('确定批量删除选中的章节吗?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        // 获取选中的ID列表
        const ids = this.sels.map(item => item.id);
        this.$http.post("/course/courseChapter/batchDelete", ids).then(res => {
          const { success, message } = res.data;
          if (success) {
            this.$message({ message: '批量删除成功', type: 'success' });
            this.getCoursesChapter();
            this.sels = [];
          } else {
            this.$message({ message: "批量删除失败[" + message + "]", type: 'error' });
          }
        }).catch(error => {
          this.$message({ message: "批量删除失败[" + error.message + "]", type: 'error' });
        });
      }).catch(() => {
        this.$message({ message: '已取消批量删除', type: 'info' });
      });
    },

    // 重置表单
    resetForm() {
      this.addForm = {
        id: "",
        name: '',
        courseId: "",
        courseName: '',
        number: ''
      };
      if (this.$refs.addForm) {
        this.$refs.addForm.resetFields();
      }
    },

    // 分页切换
    handleCurrentChange(curentPage) {
      this.page = curentPage;
      this.getCoursesChapter();
    },

    // 获取章节列表
    getCoursesChapter() {
      const para = {
        "page": this.page,
        "keyword": this.filters.keyword
      };
      this.listLoading = true;
      this.$http.post("/course/courseChapter/pagelist", para)
          .then(result => {
            this.total = result.data.data.total;
            this.coursesChapter = result.data.data.rows;
            this.listLoading = false;
          }).catch(() => {
        this.listLoading = false;
        this.$message({ message: "获取章节列表失败", type: 'error' });
      });
    },

    // 选中章节行
    chapterRowSlect(row) {
      this.chapterRow = row;
    },

    // 选择章节变化
    selsChange(sels) {
      this.sels = sels;
    }
  },
  mounted() {
    this.getCoursesChapter();
  }
}
</script>

<style scoped>
.toolbar {
  margin: 10px 0;
}
</style>