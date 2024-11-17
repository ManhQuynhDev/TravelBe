package com.quynhlm.dev.be.model.entity;

import com.quynhlm.dev.be.core.validation.ValidReactionType;

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
@Table(name = "SharePostReaction")
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor

public class SharePostReaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private int userId;
    private int shareId;
    @ValidReactionType
    private String type;
    private String create_time;
}
