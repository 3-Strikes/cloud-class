package com.example.help;

import com.example.domain.CourseOrder;
import com.example.domain.CourseOrderItem;
import lombok.Data;

import java.util.List;

@Data
public class CourseOrderWithItems {
    private CourseOrder courseOrder;
    private List<CourseOrderItem> items;
    public CourseOrderWithItems(CourseOrder courseOrder, List<CourseOrderItem> items) {
        this.courseOrder = courseOrder;
        this.items = items;
    }
}