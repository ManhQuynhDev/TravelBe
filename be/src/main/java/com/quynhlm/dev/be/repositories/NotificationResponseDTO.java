package com.quynhlm.dev.be.repositories;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponseDTO {
    private Integer id;
    private String title;
    private String message;
    private String mediaUrl;
    private String status;
    private Integer userId;
    private String fullname;
    private String avatarUrl;
    private String notificationTime;
}
