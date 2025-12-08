package com.example.dto;

import com.example.domain.Course;
import com.example.domain.CourseDetail;
import com.example.domain.CourseMarket;
import com.example.domain.CourseResource;
import jakarta.validation.Valid;

import java.util.List;

public class CourseDTO {
    @Valid
    private Course course;
    @Valid
    private CourseDetail courseDetail;
    @Valid
    private CourseMarket courseMarket;
    @Valid
    private CourseResource courseResource;

    private List<Long> teacharIds;

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public CourseDetail getCourseDetail() {
        return courseDetail;
    }

    public void setCourseDetail(CourseDetail courseDetail) {
        this.courseDetail = courseDetail;
    }

    public CourseMarket getCourseMarket() {
        return courseMarket;
    }

    public void setCourseMarket(CourseMarket courseMarket) {
        this.courseMarket = courseMarket;
    }

    public CourseResource getCourseResource() {
        return courseResource;
    }

    public void setCourseResource(CourseResource courseResource) {
        this.courseResource = courseResource;
    }

    public List<Long> getTeacharIds() {
        return teacharIds;
    }

    public void setTeacharIds(List<Long> teacharIds) {
        this.teacharIds = teacharIds;
    }
}
