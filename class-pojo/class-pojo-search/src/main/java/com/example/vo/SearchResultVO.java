package com.example.vo;

import com.example.doc.CourseDoc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SearchResultVO {
    private Long total;
    private List<CourseDoc> rows = new ArrayList<>();
    private Map<String,Object> aggResult = new HashMap<>();

    public SearchResultVO(){

    }

    public SearchResultVO(Long total, List<CourseDoc> rows, Map<String, Object> aggResult) {
        this.total = total;
        this.rows = rows;
        this.aggResult = aggResult;
    }

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

    public List<CourseDoc> getRows() {
        return rows;
    }

    public void setRows(List<CourseDoc> rows) {
        this.rows = rows;
    }

    public Map<String, Object> getAggResult() {
        return aggResult;
    }

    public void setAggResult(Map<String, Object> aggResult) {
        this.aggResult = aggResult;
    }

}
