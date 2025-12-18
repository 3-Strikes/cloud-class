package com.example.query;


/**
 *
 * @author lzy
 * @since 2025-12-04
 */
public class SystemdictionaryQuery extends BaseQuery{
    // 页码（默认第1页）
    private Integer page = 1;
    // 每页条数（默认20条，匹配前端page-size）
    private Integer rows = 20;

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        if (page != null && page > 0) {
            this.page = page;
        }
    }

    public Integer getRows() {
        return rows;
    }

    public void setRows(Integer rows) {
        if (rows != null && rows > 0 && rows <= 100) { // 限制每页最大条数
            this.rows = rows;
        }
    }

    // 关键字（匹配name字段模糊查询）
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}