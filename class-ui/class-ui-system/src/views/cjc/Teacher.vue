<template>
  <section>
    <!--工具条-->
    <el-col :span="24" class="toolbar" style="padding-bottom: 0px;">
      <el-form :inline="true" :model="filters">
        <el-form-item>
          <el-input v-model="filters.keyword" placeholder="关键字" size="small"></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" v-on:click="getTableData" icon="el-icon-search" size="small">执行查询</el-button>
        </el-form-item>
        <el-form-item>
          <el-button type="primary"  icon="el-icon-plus" size="small"  v-on:click="addTeacher" >讲师新增</el-button>
        </el-form-item>
      </el-form>
    </el-col>

    <!--列表-->
    <el-table :data="tableData" highlight-current-row v-loading="listLoading" @selection-change="selsChange" style="width: 100%;">
      <el-table-column type="selection" width="55">
      </el-table-column>
      <el-table-column  label="头像">
        <template scope="scope">
          <el-image style="width: 40px;height: 40px"
                    :src="scope.row.headImg"></el-image>
        </template>
      </el-table-column>
      <el-table-column prop="name" label="名称" >
      </el-table-column>
      <el-table-column prop="position" label="职位"  >
      </el-table-column>
      <el-table-column prop="tags" label="标签"  >
      </el-table-column>
      <el-table-column prop="intro" label="介绍" width="200" >
        <template slot-scope="scope">
          <el-popover trigger="hover" placement="top" >
            <div style="width: 400px;">{{ scope.row.intro }}</div>
            <div slot="reference" class="name-wrapper" >
              <el-tag >
                {{ scope.row.intro }}
              </el-tag>
            </div>
          </el-popover>
        </template>
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
      <el-button type="danger" @click="batchRemove" :disabled="this.sels.length===0" icon="el-icon-remove" size="small">批量删除</el-button>
      <el-pagination layout="prev, pager, next" @current-change="handleCurrentChange" :page-size="20" :total="total" style="float:right;">
      </el-pagination>
    </el-col>

    <!--新增讲师弹窗-->
    <el-dialog title="新增讲师" :visible.sync="addDialogVisible" :close-on-click-modal="false" width="500px">
      <el-form :model="addForm" label-width="80px" :rules="formRules" ref="addForm">
        <el-form-item label="姓名" prop="name">
          <el-input v-model="addForm.name"></el-input>
        </el-form-item>
        <el-form-item label="职位" prop="position">
          <el-input v-model="addForm.position"></el-input>
        </el-form-item>
        <el-form-item label="标签" prop="tags">
          <el-input v-model="addForm.tags" placeholder="多个标签用逗号分隔"></el-input>
        </el-form-item>
        <el-form-item label="介绍" prop="intro">
          <el-input v-model="addForm.intro" type="textarea" rows="4"></el-input>
        </el-form-item>
        <el-form-item label="头像">
          <el-upload
              class="avatar-uploader"
              action="/api/upload"
              :show-file-list="false"
              :on-success="handleAvatarSuccess">
            <img v-if="addForm.headImg" :src="addForm.headImg" class="avatar">
            <i v-else class="el-icon-plus avatar-uploader-icon"></i>
          </el-upload>
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="addDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitAdd">确定</el-button>
      </div>
    </el-dialog>

    <!--编辑讲师弹窗-->
    <el-dialog title="编辑讲师" :visible.sync="editDialogVisible" :close-on-click-modal="false" width="500px">
      <el-form :model="editForm" label-width="80px" :rules="formRules" ref="editForm">
        <el-form-item label="姓名" prop="name">
          <el-input v-model="editForm.name"></el-input>
        </el-form-item>
        <el-form-item label="职位" prop="position">
          <el-input v-model="editForm.position"></el-input>
        </el-form-item>
        <el-form-item label="标签" prop="tags">
          <el-input v-model="editForm.tags" placeholder="多个标签用逗号分隔"></el-input>
        </el-form-item>
        <el-form-item label="介绍" prop="intro">
          <el-input v-model="editForm.intro" type="textarea" rows="4"></el-input>
        </el-form-item>
        <el-form-item label="头像">
          <el-upload
              class="avatar-uploader"
              action="/api/upload"
              :show-file-list="false"
              :on-success="handleAvatarSuccess">
            <img v-if="editForm.headImg" :src="editForm.headImg" class="avatar">
            <i v-else class="el-icon-plus avatar-uploader-icon"></i>
          </el-upload>
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitEdit">确定</el-button>
      </div>
    </el-dialog>

  </section>
</template>

<script>
export default {
  data() {
    return {
      filters: {
        keyword: ''
      },
      tableData: [],
      total: 0,
      page: 1,
      listLoading: false,
      sels: [],
      // 新增相关
      addDialogVisible: false,
      addForm: {
        name: '',
        position: '',
        tags: '',
        intro: '',
        headImg: ''
      },
      // 编辑相关
      editDialogVisible: false,
      editForm: {},
      // 表单验证规则
      formRules: {
        name: [
          { required: true, message: '请输入讲师姓名', trigger: 'blur' }
        ],
        position: [
          { required: true, message: '请输入职位信息', trigger: 'blur' }
        ]
      }
    }
  },
  methods: {
    selsChange(sels) {
      this.sels = sels;
    },
    formatState: function(row, column) {
      return row.sex == 1 ? '禁用' : '启用';
    },
    handleCurrentChange(val) {
      this.page = val;
      this.getTableData();
    },
    getTableData() {
      let para = {
        page: this.page,
        keyword: this.filters.keyword
      };
      this.listLoading = true;
      this.$http.post("/course/teacher/pagelist", para).then(result => {
        this.total = result.data.total;
        this.tableData = result.data.data.rows;
        this.listLoading = false;
      });
    },
    // 编辑功能
    edit(row) {
      if (!row || !row.id) {
        this.$message.warning('请选择有效的讲师数据');
        return;
      }
      // 深拷贝行数据到编辑表单
      this.editForm = JSON.parse(JSON.stringify(row));
      this.editDialogVisible = true;
    },
    // 提交编辑
    submitEdit() {
      this.$refs.editForm.validate(valid => {
        if (valid) {
          this.$http.post("/course/teacher/save", this.editForm).then(result => {
            if (result.data.success) {
              this.$message.success('编辑成功');
              this.editDialogVisible = false;
              this.getTableData();
            } else {
              this.$message.error(result.data.msg || '编辑失败');
            }
          });
        }
      });
    },
    // 单个删除
    del(row) {
      this.$confirm('确认删除该讲师吗?', '提示', { type: 'warning' }).then(() => {
        this.listLoading = true;
        // 重点：将id放在URL路径中，匹配后端@PathVariable
        this.$http.delete(`/course/teacher/${row.id}`).then(result => {
          let { success, message } = result.data;
          if (success) {
            this.$message.success('删除成功');
            this.getTableData(); // 重新加载列表
          } else {
            this.$message.error(message || '删除失败');
          }
          this.listLoading = false;
        }).catch(error => {
          this.listLoading = false;
          this.$message.error(`删除失败：${error.message}`);
        });
      });
    },
    // 批量删除
    batchRemove() {
      if (this.sels.length === 0) {
        this.$message.warning('请选择需要删除的讲师');
        return;
      }
      const ids = this.sels.map(item => item.id).join(','); // 拼接id为字符串："1,2,3"
      this.$confirm('确认删除选中的讲师吗?', '提示', { type: 'warning' }).then(() => {
        this.listLoading = true;
        // 重点：批量删除ID拼接在URL路径中
        this.$http.delete(`/course/teacher/batch/${ids}`).then(result => {
          let { success, message } = result.data;
          if (success) {
            this.$message.success('批量删除成功');
            this.sels = []; // 清空选中
            this.getTableData(); // 重新加载列表
          } else {
            this.$message.error(message || '批量删除失败');
          }
          this.listLoading = false;
        }).catch(error => {
          this.listLoading = false;
          this.$message.error(`批量删除失败：${error.message}`);
        });
      });
    },
    // 新增讲师
    addTeacher() {
      // 重置表单
      this.addForm = {
        name: '',
        position: '',
        tags: '',
        intro: '',
        headImg: ''
      };
      this.$refs.addForm && this.$refs.addForm.resetFields();
      this.addDialogVisible = true;
    },
    // 提交新增
    submitAdd() {
      this.$refs.addForm.validate(valid => {
        if (valid) {
          this.$http.post("/course/teacher/save", this.addForm).then(result => {
            if (result.data.success) {
              this.$message.success('新增成功');
              this.addDialogVisible = false;
              this.getTableData();
            } else {
              this.$message.error(result.data.msg || '新增失败');
            }
          });
        }
      });
    },
    // 头像上传成功回调
    handleAvatarSuccess(res, file) {
      if (res.success) {
        // 根据当前打开的弹窗决定赋值给哪个表单
        if (this.addDialogVisible) {
          this.addForm.headImg = res.data.url;
        } else if (this.editDialogVisible) {
          this.editForm.headImg = res.data.url;
        }
      } else {
        this.$message.error('头像上传失败');
      }
    }
  },
  mounted() {
    this.getTableData();
  }
}
</script>

<style scoped>
.avatar-uploader .el-upload {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
}

.avatar-uploader .el-upload:hover {
  border-color: #409EFF;
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 178px;
  height: 178px;
  line-height: 178px;
  text-align: center;
}

.avatar {
  width: 178px;
  height: 178px;
  display: block;
}
</style>