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
@Table(name = "Story")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Story {
     @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String content;
    private int user_id;
    private String music_url;
    private String url;
    @Column(name = "create_time", updatable = false)
    private String create_time;
    private int location_id;
    private String hastag;
    @Column(name = "delFlag")
    private int delFlag;
}
