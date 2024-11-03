package com.quynhlm.dev.be.model.entity;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Set;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.validator.constraints.Length;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.quynhlm.dev.be.core.AppConstant.UserAccountRegex;
import com.quynhlm.dev.be.core.validation.StrongPassword;
import com.quynhlm.dev.be.core.validation.UserAccountElement;
import com.quynhlm.dev.be.core.validation.ValidStatusUserType;

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
    private Integer id;
    private String username;
    @StrongPassword(message = "Incorrect password format . Please try other password")
    private String password;
    @Length(min = 8, message = "name is too short . please try again !")
    private String fullname;
    private String email;
    private Set<String> roles;
    @Column(name = "phoneNumber")
    private String phoneNumber;
    private String status;
    private String dob;
    private String avatarUrl;
    private String bio;
    @CreationTimestamp
    @Column(updatable = false)
    private Timestamp create_at;
    @ValidStatusUserType
    private String isLocked;
    private LocalDateTime lastNameChangeDate;
}
