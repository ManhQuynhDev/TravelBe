package com.quynhlm.dev.be.model.entity;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.validator.constraints.Length;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.quynhlm.dev.be.core.AppConstant.UserAccountRegex;
import com.quynhlm.dev.be.core.validation.StrongPassword;
import com.quynhlm.dev.be.core.validation.UserAccountElement;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "User")
@Getter
@Setter
@RequiredArgsConstructor
@UserAccountElement.List({
        @UserAccountElement(field = "username", regex = UserAccountRegex.USERNAME, message = "username"),
        @UserAccountElement(field = "email", regex = UserAccountRegex.EMAIL, message = "email"),
        @UserAccountElement(field = "phoneNumber", regex = UserAccountRegex.PHONE_NUMBER, message = "phoneNumber"),
})
@JsonInclude(Include.NON_NULL)
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    String username;
    @StrongPassword(message = "Incorrect password format")
    String password;
    @Length(min = 8, message = "name is too short")
    String fullname;
    String email;
    String role;
    @Column(name = "phoneNumber")
    String phoneNumber;
    String status;
    String dob;
    String avatarUrl;
    String bio;
    @CreationTimestamp
    @Column(updatable = false)
    Timestamp create_at;
    LocalDateTime lastNameChangeDate;
}
