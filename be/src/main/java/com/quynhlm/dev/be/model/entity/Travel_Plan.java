package com.quynhlm.dev.be.model.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "travel_plan")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Travel_Plan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private int user_id;
    private int group_id;
    private int location_id;
    private String location;
    private String plan_name;
    private String start_date;
    private String end_date;
    private String status;
    private String description;
    private double total_butget;
    private String create_time;
}