<template>
  <section>
    <!-- 工具条：搜索+新增 -->
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
          <el-button
              type="primary"
              @click="getTableData"
              icon="el-icon-search"
              size="small"
          >查询</el-button>
        </el-form-item>
        <el-form-item>
          <el-button
              type="primary"
              icon="el-icon-plus"
              size="small"
              @click="handleAdd"
          >新增</el-button>
        </el-form-item>
      </el-form>
    </el-col>

    <!-- 数据列表 -->
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
      <el-table-column prop="sn" label="编号" min-width="100"></el-table-column>
      <el-table-column prop="name" label="标题" min-width="150"></el-table-column>
      <el-table-column prop="intro" label="简介" min-width="200"></el-table-column>
      <el-table-column
          prop="state"
          label="状态"
          width="100"
          :formatter="formatState"
          sortable
      ></el-table-column>
      <el-table-column label="操作" width="150">
        <template scope="scope">
          <el-button
              size="small"
              type="primary"
              icon="el-icon-edit"
              @click="handleEdit(scope.row)"
          >编辑</el-button>
          <el-button
              type="danger"
              size="small"
              icon="el-icon-delete"
              @click="handleDelete(scope.row.id)"
          >删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页+批量删除 -->
    <el-col :span="24" class="toolbar">
      <el-button
          type="danger"
          icon="el-icon-delete"
          :disabled="sels.length === 0"
          @click="handleBatchDelete"
      >批量删除</el-button>
      <el-pagination
          layout="prev, pager, next, jumper, ->, total"
          @current-change="handleCurrentChange"
          :page-size="20"
          :total="total"
          style="float:right;"
          :current-page="page"
      ></el-pagination>
    </el-col>

    <!-- 新增/编辑弹窗 -->
    <el-dialog
        title="字典编辑"
        :visible.sync="dialogVisible"
        width="600px"
        :close-on-click-modal="false"
    >
      <el-form
          :model="formData"
          :rules="formRules"
          ref="formRef"
          label-width="100px"
      >
        <el-form-item label="编号" prop="sn">
          <el-input v-model="formData.sn" placeholder="请输入字典编号"></el-input>
        </el-form-item>
        <el-form-item label="标题" prop="name">
          <el-input v-model="formData.name" placeholder="请输入字典标题"></el-input>
        </el-form-item>
        <el-form-item label="简介" prop="intro">
          <el-input
              v-model="formData.intro"
              type="textarea"
              rows="3"
              placeholder="请输入字典简介"
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
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </div>
    </el-dialog>
  </section>
</template>

<script>
export default {
  name: "SystemDictionary",
  data() {
    return {
      // 搜索条件
      filters: {
        keyword: ""
      },
      // 表格数据
      tableData: [],
      // 分页参数
      total: 0,
      page: 1,
      // 加载状态
      listLoading: false,
      // 选中行数据（批量删除用）
      sels: [],
      // 弹窗相关
      dialogVisible: false,
      // 表单数据
      formData: {
        id: null,
        sn: "",
        name: "",
        intro: "",
        state: 0 // 默认启用
      },
      // 表单校验规则
      formRules: {
        sn: [
          { required: true, message: "请输入字典编号", trigger: "blur" },
          { min: 2, max: 50, message: "编号长度在 2 到 50 个字符", trigger: "blur" }
        ],
        name: [
          { required: true, message: "请输入字典标题", trigger: "blur" },
          { min: 2, max: 100, message: "标题长度在 2 到 100 个字符", trigger: "blur" }
        ],
        intro: [
          { required: true, message: "请输入字典简介", trigger: "blur" }
        ],
        state: [
          { required: true, message: "请选择状态", trigger: "change" }
        ]
      }
    };
  },
  methods: {
    // 状态格式化：1=禁用，0=启用
    formatState(row, column) {
      return row.state === 1 ? "禁用" : "启用";
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
    // 获取表格数据（核心查询）
    getTableData() {
      // 构造查询参数（匹配后端SystemdictionaryQuery）
      const para = {
        page: this.page,
        rows: 20, // 每页20条，匹配前端page-size
        name: this.filters.keyword // 关键字匹配name字段模糊查询
      };
      this.listLoading = true;
      this.$http
          .post("/system/systemdictionary/pagelist", para) // 匹配后端接口路径
          .then((result) => {
            // 适配后端返回格式：JSONResult.data = PageList(total, rows)
            this.total = result.data.data.total;
            this.tableData = result.data.data.rows;
            this.listLoading = false;
          })
          .catch((error) => {
            this.listLoading = false;
            this.$message({
              message: "数据加载失败[" + error.message + "]",
              type: "error"
            });
          });
    },
    // 新增按钮
    handleAdd() {
      // 重置表单
      this.resetForm();
      this.dialogVisible = true;
    },
    // 编辑按钮
    handleEdit(row) {
      // 重置表单
      this.resetForm();
      // 回显数据
      this.formData = {
        id: row.id,
        sn: row.sn,
        name: row.name,
        intro: row.intro,
        state: row.state
      };
      this.dialogVisible = true;
    },
    // 重置表单
    resetForm() {
      this.formData = {
        id: null,
        sn: "",
        name: "",
        intro: "",
        state: 0
      };
      // 清空表单校验
      if (this.$refs.formRef) {
        this.$refs.formRef.clearValidate();
      }
    },
    // 提交新增/编辑表单
    handleSubmit() {
      // 表单校验
      this.$refs.formRef.validate((valid) => {
        if (!valid) {
          return;
        }
        // 调用保存接口
        this.$http
            .post("/system/systemdictionary/save", this.formData) // 匹配后端保存接口
            .then(() => {
              this.$message.success("操作成功");
              this.dialogVisible = false;
              // 刷新列表
              this.getTableData();
            })
            .catch((error) => {
              this.$message({
                message: "操作失败[" + error.message + "]",
                type: "error"
              });
            });
      });
    },
    // 单个删除
    handleDelete(id) {
      this.$confirm("确定删除该字典吗？", "提示", {
        type: "warning",
        confirmButtonText: "确定",
        cancelButtonText: "取消"
      })
          .then(() => {
            this.$http
                .delete(`/system/systemdictionary/${id}`) // 匹配后端删除接口
                .then(() => {
                  this.$message.success("删除成功");
                  this.getTableData();
                })
                .catch((error) => {
                  this.$message({
                    message: "删除失败[" + error.message + "]",
                    type: "error"
                  });
                });
          })
          .catch(() => {
            this.$message.info("已取消删除");
          });
    },
    // 批量删除（后端无批量接口，循环调用单个删除）
    handleBatchDelete() {
      this.$confirm("确定批量删除选中的字典吗？", "提示", {
        type: "warning"
      })
          .then(async () => {
            try {
              // 循环删除选中项
              for (const item of this.sels) {
                await this.$http.delete(`/system/systemdictionary/${item.id}`);
              }
              this.$message.success("批量删除成功");
              this.getTableData();
            } catch (error) {
              this.$message({
                message: "批量删除失败[" + error.message + "]",
                type: "error"
              });
            }
          })
          .catch(() => {
            this.$message.info("已取消批量删除");
          });
    }
  },
  mounted() {
    // 页面加载时查询数据
    this.getTableData();
  }
};
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