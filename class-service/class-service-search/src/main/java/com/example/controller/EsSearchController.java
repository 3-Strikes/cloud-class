package com.example.controller;

import com.example.dto.SearchParamDTO;
import com.example.result.JSONResult;
import com.example.service.SearchService;
import com.example.vo.SearchResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/course")
public class EsSearchController {

    @Autowired
    private SearchService searchService;

    //{total:,rows:[],aggResult:{"gradeNameTermsAgg":[{"key":"","docCount":0}],"chargeNameTermsAgg":[{"key":"","docCount":0}]}}
    @PostMapping("search")
    public JSONResult search(@RequestBody SearchParamDTO searchParam) {
        SearchResultVO searchResultVO = searchService.search(searchParam);
        return JSONResult.success(searchResultVO);
    }
}
