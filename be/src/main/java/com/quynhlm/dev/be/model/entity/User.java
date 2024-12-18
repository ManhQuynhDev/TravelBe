package com.quynhlm.dev.be.model.entity;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Set;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.validator.constraints.Length;

import com.quynhlm.dev.be.core.AppConstant.UserAccountRegex;
import com.quynhlm.dev.be.core.validation.StrongPassword;
import com.quynhlm.dev.be.core.validation.UserAccountElement;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "User")
@Getter
@Setter
@RequiredArgsConstructor
@UserAccountElement.List({
        @UserAccountElement(field = "email", regex = UserAccountRegex.EMAIL, message = "Email is not in correct format"),
        @UserAccountElement(field = "phoneNumber", regex = UserAccountRegex.PHONE_NUMBER, message = "Phone number is not in correct format")
})
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @StrongPassword(message = "Incorrect password format . Please try other password")
    private String password;
    @Pattern(regexp = "^[a-zA-Z][a-zA-Z\\s]*$", message = "Full name must start with a letter and not contain special characters")
    @Length(min = 8, message = "The full name must be at least 8 characters long")
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
    private LocalDateTime lastNameChangeDate;
}
