package com.example.config;

import com.example.doc.CourseDoc;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;
//@Controller,@Service,@Repository,@Component,@Configuration
@Repository
public interface CourseSearchRepository extends ElasticsearchRepository<CourseDoc,Long> {

}
