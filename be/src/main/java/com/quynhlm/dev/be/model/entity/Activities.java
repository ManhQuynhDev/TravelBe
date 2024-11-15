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
@Table(name = "Activities")
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Activities {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private int planId;
    private int locationId;
    private String name;
    private String time;
    private String description;
    private double cost;
    private String status;
    private String create_time;
}
