package com.example.vo;

import com.example.domain.CourseType;

import java.util.List;

public class CrumbsVO {
    private CourseType ownerProductType;//当前节点

    private List<CourseType> otherProductTypes;//当前的兄弟节点

    public CourseType getOwnerProductType() {
        return ownerProductType;
    }

    public void setOwnerProductType(CourseType ownerProductType) {
        this.ownerProductType = ownerProductType;
    }

    public List<CourseType> getOtherProductTypes() {
        return otherProductTypes;
    }

    public void setOtherProductTypes(List<CourseType> otherProductTypes) {
        this.otherProductTypes = otherProductTypes;
    }
}
