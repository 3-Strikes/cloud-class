package com.example.service;

import com.example.dto.SearchParamDTO;
import com.example.vo.SearchResultVO;

public interface SearchService {
    SearchResultVO search(SearchParamDTO searchParam);
}
