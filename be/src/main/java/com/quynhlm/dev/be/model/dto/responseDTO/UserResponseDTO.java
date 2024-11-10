package com.quynhlm.dev.be.model.dto.responseDTO;

import java.sql.Timestamp;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(Include.NON_NULL)
public class UserResponseDTO {
    private Integer id;
    private String fullname;
    private String email;
    private Set<String> roles;
    private String phoneNumber;
    private String isLocked;
    private Timestamp create_at;
}
