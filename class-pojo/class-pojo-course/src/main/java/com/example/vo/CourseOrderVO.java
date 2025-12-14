package com.example.vo;

import com.example.domain.Course;
import com.example.domain.CourseMarket;

import java.util.List;

//{totalAmount:200,courseInfos:[{"course":{},"courseMarket":{}}]}
public class CourseOrderVO {
    private Double totalAmount;
    private List<CourseAndMarket> courseInfos;

    public CourseOrderVO() {
    }

    public CourseOrderVO(Double totalAmount, List<CourseAndMarket> courseInfos) {
        this.totalAmount = totalAmount;
        this.courseInfos = courseInfos;
    }

    public static class CourseAndMarket{
        private Course course;
        private CourseMarket courseMarket;

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
    }

    public Double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public List<CourseAndMarket> getCourseInfos() {
        return courseInfos;
    }

    public void setCourseInfos(List<CourseAndMarket> courseInfos) {
        this.courseInfos = courseInfos;
    }
}
