package com.quynhlm.dev.be.model.dto.responseDTO;

import com.quynhlm.dev.be.model.entity.MessageGroup;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor

public class MessageResponseDTO {
    private MessageGroup message;
    private String mediaUrl;

    public void showInformation () {
        System.out.println(message.getContent() + " room " + message.getGroupId() + " media" + mediaUrl);
    }

}
