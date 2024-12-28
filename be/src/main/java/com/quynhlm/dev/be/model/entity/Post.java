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
@Table(name = "Post")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor

public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String content;
    private String status;
    private Integer user_id; //admin_id
    private Integer location_id;
    private Integer post_id;
    private String shareContent;
    private Integer isShare;
    private Integer share_by_id;
    private String statusShare;
    @Column(name = "create_time", updatable = false)
    private String create_time;
    private String share_time;
    private Integer delflag;
}
