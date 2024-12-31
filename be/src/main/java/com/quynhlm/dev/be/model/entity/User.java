package com.quynhlm.dev.be.model.entity;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Set;

import org.hibernate.annotations.CreationTimestamp;

import com.quynhlm.dev.be.core.AppConstant.UserAccountRegex;
import com.quynhlm.dev.be.core.validation.StrongPassword;
import com.quynhlm.dev.be.core.validation.UserAccountElement;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "User")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @StrongPassword(message = "Incorrect password format . Please try other password")
    private String password;
    private String fullname;
    private String email;
    private Set<String> roles;
    private String phoneNumber;
    private String status;
    private String dob;
    private String avatarUrl;
    private String bio;
    @CreationTimestamp
    @Column(updatable = false)
    private Timestamp create_at;
    private String isLocked;
    private String deviceToken;
    private String currentDevice;
    private LocalDateTime lockDate;
    private LocalDateTime termDate;
    private LocalDateTime lastNameChangeDate;
    private String latitude;
    private String longitude;
}
