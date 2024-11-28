package com.quynhlm.dev.be.model.dto.requestDTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class MessageSeenDTO {
    private String roomId;
    private String messageId;
    private String senderId;
    private String viewerId;
    private String status;
}