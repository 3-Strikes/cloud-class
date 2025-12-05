<template>
  <section>
    <!--工具条-->
    <el-col :span="24" class="toolbar" style="padding-bottom: 0px;">
      <el-form :inline="true" :model="filters">
        <el-form-item label="关键字">
          <el-input v-model="filters.keyword" placeholder="姓名/电话/邮箱" size="small"></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="getTableData" icon="el-icon-search" size="small">查询</el-button>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="el-icon-plus" size="small" @click="add">新增</el-button>
        </el-form-item>
      </el-form>
    </el-col>

    <!--列表：核心修改1 → 添加row-key="id"，指定唯一标识-->
    <el-table
        :data="tableData"
        highlight-current-row
        v-loading="listLoading"
        @selection-change="selsChange"
        style="width: 100%;"
        row-key="id"
    >
    <el-table-column type="selection" width="55"></el-table-column>
    <el-table-column type="index" width="60"></el-table-column>
    <el-table-column prop="realName" label="姓名" sortable>
      <!-- 处理realName为null的情况 -->
      <template scope="scope">{{ scope.row.realName || '-' }}</template>
    </el-table-column>
    <el-table-column prop="state" label="状态" :formatter="formatState" sortable></el-table-column>
    <el-table-column prop="tel" label="电话">
      <template scope="scope">{{ scope.row.tel || '-' }}</template>
    </el-table-column>
    <el-table-column prop="email" label="邮箱">
      <template scope="scope">{{ scope.row.email || '-' }}</template>
    </el-table-column>
    <el-table-column prop="inputTime" label="注册时间" :formatter="formatDate"></el-table-column>
    <el-table-column label="操作" width="200">
      <template scope="scope">
        <el-button size="small" @click="edit(scope.row)" icon="el-icon-edit" type="primary">编辑</el-button>
        <el-button type="danger" size="small" @click="del(scope.row)" icon="el-icon-close">删除</el-button>
      </template>
    </el-table-column>
    </el-table>

    <!--分页工具条-->
    <el-col :span="24" class="toolbar">
      <el-button type="danger" @click="batchRemove" :disabled="sels.length===0" icon="el-icon-close">批量删除</el-button>
      <el-pagination
          layout="prev, pager, next, jumper, ->, total"
          @current-change="selectPage"
          @size-change="handleSizeChange"
          :page-size="pageSize"
          :total="total"
          style="float:right;"
          :current-page="page">
      </el-pagination>
    </el-col>

    <!--新增/编辑弹窗-->
    <el-dialog :title="isEdit ? '编辑员工' : '新增员工'" :visible.sync="formVisible" :close-on-click-modal="false" width="500px">
      <el-form :model="form" label-width="80px" :rules="formRules" ref="formRef">
        <el-form-item label="姓名" prop="realName">
          <el-input v-model="form.realName" auto-complete="off" placeholder="请输入姓名"></el-input>
        </el-form-item>
        <el-form-item label="电话" prop="tel">
          <el-input v-model="form.tel" auto-complete="off" placeholder="请输入电话"></el-input>
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" auto-complete="off" placeholder="请输入邮箱"></el-input>
        </el-form-item>
        <el-form-item label="状态" prop="state">
          <el-select v-model="form.state" placeholder="请选择状态">
            <el-option label="正常" value="0"></el-option>
            <el-option label="锁定" value="1"></el-option>
            <el-option label="注销" value="2"></el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="formVisible = false" icon="el-icon-close">取消</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitLoading" icon="el-icon-check">提交</el-button>
      </div>
    </el-dialog>
  </section>
</template>

<script>
import util from '../../common/js/util'
export default {
  name: "Employee",
  data() {
    return {
      // 筛选条件
      filters: {
        keyword: ''
      },
      // 表格数据
      tableData: [],
      total: 0,
      page: 1, // 当前页
      pageSize: 10, // 每页条数
      listLoading: false,
      sels: [], // 选中的行

      // 弹窗相关
      formVisible: false, // 表单弹窗显示状态
      isEdit: false, // 是否为编辑模式
      submitLoading: false, // 提交加载状态

      // 表单数据（新增/编辑共用）
      form: {
        id: null,
        realName: '',
        tel: '',
        email: '',
        state: 0 // 默认正常
      },

      // 表单验证规则
      formRules: {
        realName: [
          {required: true, message: '请输入姓名', trigger: 'blur'},
          {min: 2, max: 20, message: '姓名长度在2-20个字符', trigger: 'blur'}
        ],
        tel: [
          {required: true, message: '请输入电话', trigger: 'blur'},
          {pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur'}
        ],
        email: [
          {required: true, message: '请输入邮箱', trigger: 'blur'},
          {type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur'}
        ],
        state: [
          {required: true, message: '请选择状态', trigger: 'change'}
        ]
      }
    }
  },
  methods: {
    // 选中行变化
    selsChange(sels) {
      this.sels = sels;
    },

    // 状态格式化：核心修改2 → 处理state为null的情况
    formatState(row) {
      if (row.state === null) return '未设置'; // 处理null值
      const stateMap = {0: '正常', 1: '锁定', 2: '注销'};
      return stateMap[row.state] || '未知';
    },

    // 日期格式化：核心修改3 → 兼容字符串类型的inputTime
    formatDate(row) {
      if (!row.inputTime) return '-';
      // 方案1：如果util.formatDate不兼容字符串，直接返回（推荐）
      return row.inputTime;

      // 方案2：如果需要自定义格式，手动转换（替换上面的return）
      // try {
      //   const date = new Date(row.inputTime);
      //   return `${date.getFullYear()}-${(date.getMonth()+1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')}`;
      // } catch (e) {
      //   return row.inputTime || '-';
      // }
    },

    // 页码变化
    selectPage(val) {
      this.page = val;
      this.getTableData();
    },

    // 每页条数变化
    handleSizeChange(val) {
      this.pageSize = val;
      this.page = 1; // 重置页码
      this.getTableData();
    },

    // 获取员工列表（带条件分页）：核心修改4 → 添加调试日志，确认数据赋值
    getTableData() {
      this.listLoading = true;
      const para = {
        page: this.page,
        rows: this.pageSize,
        keyword: this.filters.keyword.trim()
      };
      // 修正接口路径：网关前缀+正确的employee接口
      this.$http.post("/system/employee/pagelist", para)
          .then(result => {
            console.log("接口返回数据：", result.data); // 调试日志：确认返回数据
            const {success, data, message} = result.data;
            if (success) {
              this.total = data.total;
              this.tableData = data.rows;
              console.log("赋值后的tableData：", this.tableData); // 调试日志：确认数据赋值
              // 强制触发响应式更新（兜底方案）
              this.$forceUpdate();
            } else {
              this.$message.error(`数据加载失败：${message}`);
            }
          })
          .catch(error => {
            console.error("接口请求失败：", error); // 调试日志：捕获错误
            this.$message.error(`数据加载失败：${error.message}`);
          })
          .finally(() => {
            this.listLoading = false;
          });
    },

    // 单个删除
    del(row) {
      this.$confirm('确认删除该员工吗？', '温馨提示', {
        type: 'warning',
        confirmButtonText: '确认',
        cancelButtonText: '取消'
      }).then(() => {
        this.listLoading = true;
        this.$http.delete(`/system/employee/${row.id}`)
            .then(result => {
              const {success, message} = result.data;
              if (success) {
                this.$message.success('删除成功');
                this.getTableData();
              } else {
                this.$message.error(`删除失败：${message}`);
              }
            })
            .catch(error => {
              this.$message.error(`删除失败：${error.message}`);
            })
            .finally(() => {
              this.listLoading = false;
            });
      }).catch(() => {
        this.$message.info('已取消删除');
      });
    },

    // 批量删除
    batchRemove() {
      if (this.sels.length === 0) return;
      const ids = this.sels.map(item => item.id).join(',');
      this.$confirm('确认删除选中的员工吗？', '温馨提示', {type: 'warning'})
          .then(() => {
            this.listLoading = true;
            this.$http.delete(`/system/employee/batch/${ids}`)
                .then(result => {
                  const {success, message} = result.data;
                  if (success) {
                    this.$message.success('批量删除成功');
                    this.getTableData();
                  } else {
                    this.$message.error(`批量删除失败：${message}`);
                  }
                })
                .catch(error => {
                  this.$message.error(`批量删除失败：${error.message}`);
                })
                .finally(() => {
                  this.listLoading = false;
                });
          }).catch(() => {
        this.$message.info('已取消批量删除');
      });
    },

    // 打开新增弹窗
    add() {
      // 重置表单
      this.resetForm();
      this.isEdit = false;
      this.formVisible = true;
    },

    // 打开编辑弹窗
    edit(row) {
      // 重置表单
      this.resetForm();
      this.isEdit = true;
      // 赋值表单数据（字段一一对应）
      this.form = {...row};
      // 处理state为null的情况
      if (this.form.state === null) {
        this.form.state = 0; // 默认设为正常
      }
      this.formVisible = true;
    },

    // 重置表单
    resetForm() {
      this.form = {
        id: null,
        realName: '',
        tel: '',
        email: '',
        state: 0
      };
      // 重置表单验证状态
      if (this.$refs.formRef) {
        this.$refs.formRef.clearValidate();
      }
    },

    // 提交新增/编辑表单
    submitForm() {
      this.$refs.formRef.validate((valid) => {
        if (!valid) return;

        this.submitLoading = true;
        // 统一调用save接口（后端已兼容新增/编辑）
        this.$http.post("/system/employee/save", this.form)
            .then(result => {
              const {success, message} = result.data;
              if (success) {
                this.$message.success(this.isEdit ? '编辑成功' : '新增成功');
                this.formVisible = false;
                this.getTableData(); // 刷新列表
              } else {
                this.$message.error(`${this.isEdit ? '编辑' : '新增'}失败：${message}`);
              }
            })
            .catch(error => {
              this.$message.error(`${this.isEdit ? '编辑' : '新增'}失败：${error.message}`);
            })
            .finally(() => {
              this.submitLoading = false;
            });
      });
    }
  },
  mounted() {
    // 初始化加载列表
    this.getTableData();
  }
}
</script>

<style scoped>
.toolbar {
  margin: 10px 0;
}

.dialog-footer {
  text-align: center;
}
</style>