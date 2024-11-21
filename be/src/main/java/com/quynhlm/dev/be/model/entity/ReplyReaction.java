package com.quynhlm.dev.be.model.entity;

import com.quynhlm.dev.be.core.validation.ValidReactionType;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "ReplyReaction")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ReplyReaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private int userId;
    private int replyId;
    @ValidReactionType
    private String type;
    private String create_time;
}
