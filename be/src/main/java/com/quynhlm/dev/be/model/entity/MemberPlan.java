package com.quynhlm.dev.be.model.entity;

import jakarta.persistence.Column;
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
@Table(name = "MemberPlan")
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class MemberPlan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(name = "planId")
    private Integer planId;
    @Column(name = "userId")
    private Integer userId;
    private String join_time;
    private String role;
    private String status;  
}
