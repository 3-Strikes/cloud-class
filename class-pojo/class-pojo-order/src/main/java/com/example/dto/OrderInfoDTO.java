package com.example.dto;

import lombok.Data;

import java.util.List;

@Data
public class OrderInfoDTO {
    private Long userId;
    private List<Long> courseIds;
}
