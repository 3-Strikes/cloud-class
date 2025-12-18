<template>
  <section>
    <!--工具条：搜索+新增-->
    <el-col :span="24" class="toolbar" style="padding-bottom: 0px;">
      <el-form :inline="true" :model="filters">
        <el-form-item label="关键字">
          <el-input
              v-model="filters.keyword"
              placeholder="请输入标题关键字"
              size="small"
              @keyup.enter="getTableData"
          ></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" v-on:click="getTableData" icon="el-icon-search" size="small">查询</el-button>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="el-icon-plus" size="small" @click="handleAdd">新增</el-button>
        </el-form-item>
      </el-form>
    </el-col>

    <!--列表：补充选中事件、样式优化-->
    <el-table
        :data="tableData"
        highlight-current-row
        v-loading="listLoading"
        @selection-change="handleSelectionChange"
        style="width: 100%; margin: 10px 0;"
        border
    >
      <el-table-column type="selection" width="55"></el-table-column>
      <el-table-column type="index" width="60" label="序号"></el-table-column>
      <el-table-column prop="name" label="标题" min-width="150"></el-table-column>
      <el-table-column prop="intro" label="简介" min-width="200"></el-table-column>
      <!-- 兼容无state字段的情况，默认显示启用 -->
      <el-table-column label="状态" width="100" :formatter="formatState" sortable></el-table-column>
      <el-table-column label="操作" width="150">
        <template scope="scope">
          <el-button size="small" type="primary" @click="edit(scope.row)">编辑</el-button>
          <el-button type="danger" size="small" @click="del(scope.row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!--工具条：批量删除+分页优化-->
    <el-col :span="24" class="toolbar">
      <el-button
          type="danger"
          icon="el-icon-delete"
          :disabled="sels.length===0"
          @click="handleBatchDelete"
          size="small"
      >批量删除</el-button>
      <el-pagination
          layout="prev, pager, next, jumper, ->, total"
          @current-change="handleCurrentChange"
          :page-size="20"
          :total="total"
          style="float:right;"
          :current-page="page"
          size="small"
      ></el-pagination>
    </el-col>

    <!-- 新增/编辑弹窗 -->
    <el-dialog
        title="字典项编辑"
        :visible.sync="dialogVisible"
        width="600px"
        :close-on-click-modal="false"
    >
      <el-form
          :model="formData"
          :rules="formRules"
          ref="formRef"
          label-width="100px"
          size="small"
      >
        <el-form-item label="标题" prop="name">
          <el-input v-model="formData.name" placeholder="请输入字典项标题"></el-input>
        </el-form-item>
        <el-form-item label="简介" prop="intro">
          <el-input
              v-model="formData.intro"
              type="textarea"
              rows="3"
              placeholder="请输入字典项简介"
          ></el-input>
        </el-form-item>
        <el-form-item label="状态" prop="state">
          <el-radio-group v-model="formData.state">
            <el-radio label="0">启用</el-radio>
            <el-radio label="1">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button size="small" @click="dialogVisible = false">取消</el-button>
        <el-button size="small" type="primary" @click="handleSubmit">确定</el-button>
      </div>
    </el-dialog>
  </section>
</template>

<script>
export default {
  name: "SystemDictionaryItem",
  data() {
    return {
      // 搜索条件
      filters: {
        keyword: ''
      },
      // 表格数据
      tableData: [],
      // 分页参数
      total: 0,
      page: 1,
      // 加载状态
      listLoading: false,
      // 选中行（批量删除用）
      sels: [],
      // 弹窗显隐
      dialogVisible: false,
      // 表单数据
      formData: {
        id: null,
        name: '',
        intro: '',
        state: 0 // 默认启用
      },
      // 表单校验规则
      formRules: {
        name: [
          {required: true, message: "请输入字典项标题", trigger: "blur"},
          {min: 2, max: 100, message: "标题长度2-100字符", trigger: "blur"}
        ],
        intro: [
          {required: true, message: "请输入字典项简介", trigger: "blur"}
        ],
        state: [
          {required: true, message: "请选择状态", trigger: "change"}
        ]
      }
    }
  },
  methods: {
    // 状态格式化：修正sex→state，兼容无state字段
    formatState: function (row, column) {
      return row.state === 1 ? '禁用' : '启用';
    },
    // 分页切换
    handleCurrentChange(val) {
      this.page = val;
      this.getTableData();
    },
    // 选中行变化（批量操作）
    handleSelectionChange(val) {
      this.sels = val;
    },
    // 重置表单（新增/编辑前）
    resetForm() {
      this.formData = {
        id: null,
        name: '',
        intro: '',
        state: 0
      };
      if (this.$refs.formRef) {
        this.$refs.formRef.clearValidate();
      }
    },
    // 新增按钮
    handleAdd() {
      this.resetForm();
      this.dialogVisible = true;
    },
    // 获取表格数据（核心：适配后端接口+参数+返回格式）
    getTableData() {
      this.listLoading = true;
      // 构造参数：匹配后端SystemdictionaryitemQuery（page/rows/name）
      let para = {
        page: this.page,
        rows: 20, // 每页20条，匹配后端分页参数
        name: this.filters.keyword // 关键字映射到name字段（后端模糊查询）
      };
      // 修正接口路径：匹配后端@RequestMapping("/system/systemdictionaryitem")
      this.$http.post("/system/systemdictionaryitem/pagelist", para).then(result => {
        let {success, data, message, code} = result.data;
        // 适配后端JSONResult返回格式（success=true为成功）
        if (success) {
          this.total = data.total;
          this.tableData = data.rows;
        } else {
          this.$message({message: "数据加载失败[" + message + "]", type: 'error'});
        }
        this.listLoading = false;
      }).catch(error => {
        this.listLoading = false;
        this.$message({message: "数据加载失败[" + error.message + "]", type: 'error'});
      })
    },
    // 编辑方法
    edit(row) {
      this.resetForm();
      // 调用后端get接口获取单条数据
      this.$http.get(`/system/systemdictionaryitem/${row.id}`).then(res => {
        if (res.data.success) {
          // 回显数据，兼容无state字段的情况
          this.formData = {
            ...res.data.data,
            state: res.data.data.state || 0
          };
          this.dialogVisible = true;
        } else {
          this.$message.error("获取数据失败：" + res.data.message);
        }
      }).catch(err => {
        this.$message.error("获取数据失败：" + err.message);
      });
    },
    // 单个删除
    del(id) {
      this.$confirm('确定删除该字典项吗？', '提示', {
        type: 'warning',
        confirmButtonText: '确定',
        cancelButtonText: '取消'
      }).then(() => {
        // 调用后端delete接口
        this.$http.delete(`/system/systemdictionaryitem/${id}`).then(res => {
          if (res.data.success) {
            this.$message.success("删除成功");
            this.getTableData(); // 删除后刷新列表
          } else {
            this.$message.error("删除失败：" + res.data.message);
          }
        }).catch(err => {
          this.$message.error("删除失败：" + err.message);
        });
      }).catch(() => {
        this.$message.info("已取消删除");
      });
    },
    // 提交新增/编辑表单
    handleSubmit() {
      this.$refs.formRef.validate((valid) => {
        if (!valid) return;
        // 调用后端save接口（新增/修改共用）
        this.$http.post("/system/systemdictionaryitem/save", this.formData).then(res => {
          if (res.data.success) {
            this.$message.success("操作成功");
            this.dialogVisible = false;
            this.getTableData(); // 操作后刷新列表
          } else {
            this.$message.error("操作失败：" + res.data.message);
          }
        }).catch(err => {
          this.$message.error("操作失败：" + err.message);
        });
      });
    },
    // 批量删除（后端无批量接口，循环调用单个删除）
    handleBatchDelete() {
      this.$confirm('确定批量删除选中的字典项吗？', '提示', {
        type: 'warning'
      }).then(async () => {
        try {
          // 循环删除选中项
          for (const item of this.sels) {
            await this.$http.delete(`/system/systemdictionaryitem/${item.id}`);
          }
          this.$message.success("批量删除成功");
          this.getTableData();
        } catch (error) {
          this.$message.error("批量删除失败[" + error.message + "]");
        }
      }).catch(() => {
        this.$message.info("已取消批量删除");
      });
    }
  },
  mounted() {
    // 页面加载初始化数据
    this.getTableData();
  }
}
</script>

<style scoped>
.toolbar {
  padding: 10px;
  background: #fff;
  margin-bottom: 10px;
}

.dialog-footer {
  text-align: right;
}

.el-table {
  --el-table-header-text-color: #333;
  --el-table-row-hover-bg-color: #f5f7fa;
}
</style>