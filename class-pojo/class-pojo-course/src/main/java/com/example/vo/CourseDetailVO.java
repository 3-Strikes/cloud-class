package com.example.vo;

import com.example.domain.*;

import java.util.List;

public class CourseDetailVO {
    private Course course;
    private CourseMarket courseMarket;
    private List<CourseChapter> courseChapters;//{id:1,name:"",mediaFiles:[]}

    private List<Teacher> teachers;

    private CourseDetail courseDetail;

    private CourseSummary courseSummary;

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public CourseMarket getCourseMarket() {
        return courseMarket;
    }

    public void setCourseMarket(CourseMarket courseMarket) {
        this.courseMarket = courseMarket;
    }

    public List<CourseChapter> getCourseChapters() {
        return courseChapters;
    }

    public void setCourseChapters(List<CourseChapter> courseChapters) {
        this.courseChapters = courseChapters;
    }

    public List<Teacher> getTeachers() {
        return teachers;
    }

    public void setTeachers(List<Teacher> teachers) {
        this.teachers = teachers;
    }

    public CourseDetail getCourseDetail() {
        return courseDetail;
    }

    public void setCourseDetail(CourseDetail courseDetail) {
        this.courseDetail = courseDetail;
    }

    public CourseSummary getCourseSummary() {
        return courseSummary;
    }

    public void setCourseSummary(CourseSummary courseSummary) {
        this.courseSummary = courseSummary;
    }
}
