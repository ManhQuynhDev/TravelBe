package com.quynhlm.dev.be.model.dto.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor

public class UserInvitationResponseDTO {
    private Integer user_id;
    private Integer group_id;
    private String fullname;
    private String avatar_url;
    private String group_name;
    private String bio;
    private String cover_photo;
    private String admin_name;
    private String admin_avatar;
}
