package com.quynhlm.dev.be.model.dto.responseDTO;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UserResponseDTO {
    private Integer id;
    private String fullname;
    private String email;
    private Set<String> roles;
    private String phoneNumber;
    private String dob;
    private String bio;
    private String isLocked;
    private String avatarUrl;
    private LocalDateTime lastNameChangeDate;
    private Timestamp create_at;
}
