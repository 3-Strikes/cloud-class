<template id="courseType">
  <div id="courseType" style="padding: 10px; height: 100%; box-sizing: border-box;">
    <!-- 使用Element UI标准Row/Col布局，彻底解决float布局错乱问题 -->
    <el-row :gutter="10" style="height: calc(100% - 20px);">
      <!-- 左侧分类树：占5列（约20%） -->
      <el-col :span="5">
        <div style="border: 1px solid #eeeeee; border-radius: 5px; height: 100%; padding: 10px; box-sizing: border-box;">
          <el-tree
              :data="courseTypes"
              :props="defaultProps"
              @node-click="handleNodeClick"
              node-key="id"
              default-expand-all
              style="height: 100%; overflow-y: auto;">
          </el-tree>
        </div>
      </el-col>

      <!-- 右侧操作区：占19列（约80%） -->
      <el-col :span="19">
        <div style="border: 1px solid #eeeeee; border-radius: 5px; height: 100%; display: flex; flex-direction: column; box-sizing: border-box;">
          <!-- 顶部查询工具栏 -->
          <div class="toolbar" style="padding: 10px; border-bottom: 1px solid #eee; flex-shrink: 0;">
            <el-form :inline="true" :model="filters" size="small">
              <el-form-item label="关键字：">
                <el-input
                    v-model="filters.keyword"
                    placeholder="请输入分类名称/描述"
                    clearable
                    style="width: 200px;">
                </el-input>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="getList" icon="el-icon-search">查询分类</el-button>
              </el-form-item>
              <el-form-item>
                <el-button type="success" @click="handleAdd" icon="el-icon-plus">新增分类</el-button>
              </el-form-item>
            </el-form>
          </div>

          <!-- 中间列表区域（自适应高度） -->
          <div style="flex: 1; overflow-y: auto; padding: 10px;">
            <el-table
                :data="datas"
                highlight-current-row
                @selection-change="selsChange"
                style="width: 100%;"
                v-loading="listLoading"
                border>
              <el-table-column type="selection" width="55"></el-table-column>
              <el-table-column prop="name" label="分类标题" width="200" sortable></el-table-column>
              <el-table-column prop="description" label="描述" min-width="200"></el-table-column>
              <el-table-column prop="totalCount" label="课程数" width="100" sortable></el-table-column>
              <el-table-column prop="pName" label="上级分类" width="120" sortable></el-table-column>
              <el-table-column prop="sortIndex" label="排序" width="80" sortable></el-table-column>
              <el-table-column label="操作" width="180">
                <template slot-scope="scope">
                  <el-button
                      size="mini"
                      @click="handleEdit(scope.row)"
                      icon="el-icon-edit"
                      type="primary">
                    编辑
                  </el-button>
                  <el-button
                      size="mini"
                      @click="handleDel(scope.row)"
                      icon="el-icon-delete"
                      type="danger">
                    删除
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>

          <!-- 底部分页/批量操作栏 -->
          <div class="toolbar" style="padding: 10px; border-top: 1px solid #eee; flex-shrink: 0; display: flex; justify-content: space-between; align-items: center;">
            <el-button
                type="danger"
                @click="batchRemove"
                :disabled="sels.length===0"
                icon="el-icon-delete"
                size="small">
              批量删除
            </el-button>
            <el-pagination
                layout="prev, pager, next, jumper, ->, total"
                @current-change="handleCurrentChange"
                :current-page="page"
                :page-size="pageSize"
                :total="total"
                style="margin: 0;">
            </el-pagination>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 新增/编辑弹窗（复用） -->
    <el-dialog
        :title="isEdit ? '编辑分类' : '新增分类'"
        :visible.sync="formVisible"
        :close-on-click-modal="false"
        width="500px">
      <el-form
          :model="form"
          label-width="100px"
          ref="formRef"
          :rules="formRules">
        <el-form-item label="分类标题" prop="name">
          <el-input v-model="form.name" placeholder="请输入分类标题"></el-input>
        </el-form-item>
        <el-form-item label="LOGO地址" prop="logo">
          <el-input v-model="form.logo" placeholder="选填，输入LOGO图片URL"></el-input>
        </el-form-item>
        <el-form-item label="排序值" prop="sortIndex">
          <el-input v-model="form.sortIndex" type="number" placeholder="数字越大排序越靠前"></el-input>
        </el-form-item>
        <el-form-item label="分类描述" prop="description">
          <el-input
              v-model="form.description"
              type="textarea"
              rows="3"
              placeholder="请输入分类描述">
          </el-input>
        </el-form-item>
        <el-form-item label="上级分类ID" prop="pid">
          <el-input
              v-model="form.pid"
              type="number"
              placeholder="顶级分类填0，否则填上级分类ID">
          </el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm" >提交</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<style scoped>
/* 彻底重置样式，解决布局错乱 */
#courseType {
  height: 100%;
  box-sizing: border-box;
}
.toolbar {
  margin: 0;
  padding: 10px;
}
.el-row {
  height: 100%;
  margin: 0 !important;
}
.el-col {
  height: 100%;
  box-sizing: border-box;
}
/* 去掉原来错误的红色边框 */
::v-deep #courseType .el-col {
  border: none !important;
}
</style>

<script>
export default {
  data() {
    return {
      // 表单相关
      formVisible: false, // 弹窗显示状态
      isEdit: false, // 是否编辑模式
      form: { // 新增/编辑表单数据
        id: "",
        name: "",
        logo: "",
        sortIndex: 0,
        description: "",
        pid: 0
      },
      formRules: { // 表单验证规则
        name: [
          { required: true, message: '分类标题不能为空', trigger: 'blur' },
          { min: 2, max: 50, message: '标题长度2-50个字符', trigger: 'blur' }
        ],
        sortIndex: [
          { required: true, message: '排序值不能为空', trigger: 'blur' },
          { type: 'number', message: '排序值必须为数字', trigger: 'blur' }
        ],
        pid: [
          { type: 'number', message: '上级分类ID必须为数字', trigger: 'blur' }
        ]
      },
      // 列表相关
      sels: [], // 选中的行
      filters: { keyword: "" }, // 查询条件
      datas: [], // 列表数据
      page: 1, // 当前页
      pageSize: 10, // 每页条数（匹配后端）
      total: 0, // 总条数
      listLoading: false, // 加载状态
      // 树形菜单相关
      courseTypes: [], // 分类树形数据
      defaultProps: {
        children: 'children',
        label: 'name'
      }
    }
  },
  methods: {
    // ========== 核心修复：查询功能（解决不可用问题） ==========
    getList() {
      this.listLoading = true; // 显示加载圈
      // 构造查询参数（补充pageSize，匹配后端CourseTypeQuery的rows参数）
      let para = {
        page: this.page,
        rows: this.pageSize, // 关键：补充每页条数，修复查询参数缺失问题
        keyword: this.filters.keyword || ""
      };
      // 分页查询（修复接口路径：后端@RequestMapping是/courseType，前端补全前缀/course）
      this.$http.post("/course/courseType/pagelist", para)
          .then(result => {
            // 兼容后端返回格式
            if (result.data && result.data.success !== false) {
              this.total = result.data.data.total || 0;
              this.datas = result.data.data.rows || [];
            } else {
              this.$message.error("查询失败：" + (result.data.message || "数据格式异常"));
              this.datas = [];
              this.total = 0;
            }
            this.listLoading = false;
          })
          .catch(error => {
            this.$message.error("查询异常：" + error.message);
            this.listLoading = false;
          });
    },

    // ========== 选中行变化 ==========
    selsChange(val) {
      this.sels = val; // 修复：绑定选中行数据
    },

    // ========== 分页切换 ==========
    handleCurrentChange(val) {
      this.page = val;
      this.getList(); // 切换页码后重新查询
    },

    // ========== 树形节点点击 ==========
    handleNodeClick(row) {
      this.form.pid = row.id; // 设置新增的上级分类ID
      // 加载当前分类下的子分类
      this.getChildrenByPid(row.id);
    },

    // ========== 根据PID查询子分类 ==========
    getChildrenByPid(pid) {
      this.listLoading = true;
      this.$http.get(`/course/courseType/selectChildrenById/${pid}`)
          .then(res => {
            if (res.data && res.data.success !== false) {
              this.datas = res.data.data || [];
              this.total = this.datas.length; // 子分类列表不分页
            } else {
              this.$message.warning("获取子分类失败：" + (res.data.message || "无数据"));
              this.datas = [];
            }
            this.listLoading = false;
          })
          .catch(error => {
            this.$message.error("请求异常：" + error.message);
            this.listLoading = false;
          });
    },

    // ========== 获取分类树形数据 ==========
    getTreeData() {
      this.$http.get("/course/courseType/treeData")
          .then(res => {
            if (res.data && res.data.success !== false) {
              this.courseTypes = res.data.data || [];
            } else {
              this.$message.warning("加载分类树失败：" + (res.data.message || "无数据"));
              this.courseTypes = [];
            }
          })
          .catch(error => {
            this.$message.error("加载分类树异常：" + error.message);
          });
    },

    // ========== 新增分类 ==========
    handleAdd() {
      this.isEdit = false;
      // 重置表单
      this.form = {
        id: "",
        name: "",
        logo: "",
        sortIndex: 0,
        description: "",
        pid: 0 // 默认顶级分类
      };
      this.formVisible = true;
      // 重置表单验证
      this.$nextTick(() => {
        this.$refs.formRef.resetFields();
      });
    },

    // ========== 编辑分类 ==========
    handleEdit(row) {
      if (!row || !row.id) {
        this.$message.warning("请选择有效的分类！");
        return;
      }
      this.isEdit = true;
      // 获取分类详情
      this.$http.get(`/course/courseType/${row.id}`)
          .then(res => {
            if (res.data && res.data.success !== false) {
              this.form = { ...res.data.data }; // 赋值表单
              this.formVisible = true;
              // 重置验证
              this.$nextTick(() => {
                this.$refs.formRef.resetFields();
              });
            } else {
              this.$message.error("获取分类详情失败：" + (res.data.message || "无数据"));
            }
          })
          .catch(error => {
            this.$message.error("请求异常：" + error.message);
          });
    },

    // ========== 提交表单（新增/编辑） ==========
    submitForm() {
      this.$refs.formRef.validate((valid) => {
        if (valid) {
          // 调用保存接口（新增/编辑复用）
          this.$http.post("/course/courseType/save", this.form)
              .then(res => {
                if (res.data && res.data.success !== false) {
                  this.formVisible = false;
                  this.$message.success(this.isEdit ? "编辑成功" : "新增成功");
                  this.getList(); // 刷新列表
                  this.getTreeData(); // 刷新树形菜单
                } else {
                  this.$message.error((this.isEdit ? "编辑" : "新增") + "失败：" + (res.data.message || "操作异常"));
                }
              })
              .catch(error => {
                this.$message.error("请求异常：" + error.message);
              });
        }
      });
    },

    // ========== 单个删除 ==========
    handleDel(row) {
      if (!row || !row.id) {
        this.$message.warning("请选择有效的分类！");
        return;
      }
      this.$confirm('此操作将永久删除该分类，是否继续？', '警告', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        // 匹配后端DELETE /courseType/{id} 接口
        this.$http.delete(`/course/courseType/${row.id}`)
            .then(res => {
              if (res.data && res.data.success !== false) {
                this.$message.success("删除成功");
                this.getList(); // 刷新列表
                this.getTreeData(); // 刷新树形
              } else {
                this.$message.error("删除失败：" + (res.data.message || "操作异常"));
              }
            })
            .catch(error => {
              this.$message.error("删除异常：" + error.message);
            });
      }).catch(() => {
        this.$message.info("已取消删除");
      });
    },

    // ========== 批量删除 ==========
    batchRemove() {
      if (this.sels.length === 0) {
        this.$message.warning("请选择要删除的分类！");
        return;
      }
      this.$confirm(`确定删除选中的${this.sels.length}个分类吗？`, '警告', {
        type: 'warning'
      }).then(() => {
        // 拼接ID字符串：1,2,3
        const ids = this.sels.map(item => item.id).join(',');
        // 调用批量删除接口
        this.$http.delete(`/course/courseType/batch/${ids}`)
            .then(res => {
              if (res.data && res.data.success !== false) {
                this.$message.success("批量删除成功");
                this.sels = []; // 清空选中
                this.getList(); // 刷新列表
                this.getTreeData(); // 刷新树形
              } else {
                this.$message.error("批量删除失败：" + (res.data.message || "操作异常"));
              }
            })
            .catch(error => {
              this.$message.error("批量删除异常：" + error.message);
            });
      }).catch(() => {
        this.$message.info("已取消批量删除");
      });
    }
  },
  mounted() {
    // 页面加载初始化
    this.getList(); // 加载列表
    this.getTreeData(); // 加载分类树
  }
};
</script>