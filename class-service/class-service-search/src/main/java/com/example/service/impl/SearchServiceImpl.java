package com.example.service.impl;

import cn.hutool.core.util.StrUtil;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.aggregations.*;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.JsonData;
import com.example.constant.Constants;
import com.example.doc.CourseDoc;
import com.example.dto.SearchParamDTO;
import com.example.exceptions.BusinessException;
import com.example.service.SearchService;
import com.example.vo.SearchResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SearchServiceImpl implements SearchService {

    @Autowired
    private ElasticsearchClient searchClient;
    @Override
    public SearchResultVO search(SearchParamDTO searchParam) {
        SearchRequest.Builder builder = new SearchRequest.Builder().index(Constants.COURSE_INDEX);

        //        模糊匹配查询条件--------begin
        BoolQuery.Builder boolBuilder = new BoolQuery.Builder();
        if(StrUtil.isNotEmpty(searchParam.getKeyword())){
            boolBuilder.must(m->m.multiMatch(f->f.query(searchParam.getKeyword()).fields("name","forUser","teacherNames")));
        }
        //        模糊匹配查询条件--------end

        //        filter过滤条件----------begin
        if(StrUtil.isNotEmpty(searchParam.getChargeName())){
            boolBuilder.filter(f->f.term(t->t.field("charge").value(searchParam.getChargeName())));
        }

        if(searchParam.getCourseTypeId()!=null){
            boolBuilder.filter(f->f.term(t->t.field("courseTypeId").value(searchParam.getCourseTypeId())));
        }

        if(StrUtil.isNotEmpty(searchParam.getGradeName())){
            boolBuilder.filter(f->f.term(t->t.field("gradeName.keyword").value(searchParam.getGradeName())));
        }

        if(searchParam.getPriceMax()!=null){
            boolBuilder.filter(f->f.range(r-> r.field("price").lte(JsonData.of(searchParam.getPriceMax()))));
        }

        if(searchParam.getPriceMin()!=null){
            boolBuilder.filter(f->f.range(r-> r.field("price").gte(JsonData.of(searchParam.getPriceMin()))));
        }

        builder.query(boolBuilder.build()._toQuery());
        //查询过滤条件

        //分页
        builder.from((searchParam.getPage()-1)*searchParam.getRows());
        builder.size(searchParam.getRows());


        String sortField = "price";

        if(StrUtil.isNotEmpty(searchParam.getSortField())){
            if("xl".equals(searchParam.getSortField())) sortField = "saleCount";
            else if("xp".equals(searchParam.getSortField())) sortField = "price";
            else if("pl".equals(searchParam.getSortField())) sortField = "commentConut";
            else if("jg".equals(searchParam.getSortField())) sortField = "price";
            else if("rq".equals(searchParam.getSortField())) sortField = "viewCount";
            else sortField = "price";
        }
        SortOrder sortType = SortOrder.Desc;
        String sortTypeStr = searchParam.getSortType();
        if(StrUtil.isNotEmpty(sortTypeStr)){
            if("asc".equals(sortTypeStr)) sortType = SortOrder.Asc;
            else sortType = SortOrder.Desc;
        }

        SortOrder finalSortType = sortType;
        String finalSortField = sortField;
        builder.sort(f->f.field(s->s.field(finalSortField).order(finalSortType)));
        //排序字段

        //设置高亮字段
        builder.highlight(h->h.fields("name",f->f.preTags("<span style='color:red'>").postTags("</span>"))
                .fields("teacherNames",f->f.preTags("<span style='color:red'>").postTags("</span>")));

        //聚合查询
        builder.aggregations("grade_terms",a->a.terms(t->t.field("gradeName.keyword")));
        builder.aggregations("charge_terms",a->a.terms(t->t.field("charge")));

        try {
            SearchResultVO result=new SearchResultVO();
            SearchResponse<CourseDoc> searchRepos = searchClient.search(builder.build(), CourseDoc.class);
            //结果集封装------------begin
            result.setTotal(searchRepos.hits().total().value());
            List<Hit<CourseDoc>> hits = searchRepos.hits().hits();
            List<CourseDoc> rows=new ArrayList<>();
            for (Hit<CourseDoc> hit : hits) {
                CourseDoc source = hit.source();
                Map<String, List<String>> highlight = hit.highlight();//高亮字段集合
                if(highlight.containsKey("name")){
                    source.setName(highlight.get("name").get(0));
                }
                if(highlight.containsKey("teacherNames")){
                    source.setTeacherNames(highlight.get("teacherNames").get(0));
                }
                rows.add(source);
            }
            result.setRows(rows);
            //结果集封装------------end

            //对聚合结果进行处理-----------begin
            Map<String, Aggregate> aggregations = searchRepos.aggregations();

            StringTermsAggregate gradeTerms = aggregations.get("grade_terms").sterms();
            List<StringTermsBucket> array = gradeTerms.buckets().array();

            List<Map<String,Object>> gradeTermsList = new ArrayList<>();//[{"key":"青铜","docCount":1},{"key":"白银","docCount":1}]
            for (StringTermsBucket b : array) {
                Map<String,Object> gradeTermsMap = new HashMap<>();
                gradeTermsMap.put("key",b.key().stringValue());
                gradeTermsMap.put("docCount",b.docCount());
                gradeTermsList.add(gradeTermsMap);
            }
            result.getAggResult().put("gradeNameTermsAgg",gradeTermsList);//{aggResult:{"gradeNameTermsAgg":[{"key":"","docCount":0}]}

            List<Map<String,Object>> chargeTermsList = new ArrayList<>();
            LongTermsAggregate chargeTerms = aggregations.get("charge_terms").lterms();
            List<LongTermsBucket> array2 = chargeTerms.buckets().array();
            for (LongTermsBucket longTermsBucket : array2) {
                Map<String,Object> chargeTermsMap = new HashMap<>();
                chargeTermsMap.put("key",longTermsBucket.key());
                chargeTermsMap.put("docCount",longTermsBucket.docCount());
                chargeTermsList.add(chargeTermsMap);
            }
            result.getAggResult().put("chargeNameTermsAgg",chargeTermsList);//{aggResult:{"chargeNameTermsAgg":[{"key":"","docCount":0}]}
            //对聚合结果进行处理-----------end

            return result;
        } catch (IOException e) {
            throw new BusinessException("网络异常，稍候重试");
        }



    }
}
