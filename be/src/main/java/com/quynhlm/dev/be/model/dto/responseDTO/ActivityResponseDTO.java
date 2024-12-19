package com.quynhlm.dev.be.model.dto.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

public class ActivityResponseDTO {
    private Integer id;
    private Integer userId;
    private Integer planId;
    private Integer locationId;
    private String activity_name;
    private String fullname;
    private String avatarUrl;
    private String planName;
    private String description;
    private String time;
    private Double cost;
    private String status; 
    private String create_time;
}