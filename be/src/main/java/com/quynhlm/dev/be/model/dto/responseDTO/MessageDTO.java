package com.quynhlm.dev.be.model.dto.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

public class MessageDTO{
    private Integer sender;
    private Integer receiver;
    private String message;
    private String file;

    @Override
    public String toString() {
        return "MessageDTO [sender=" + sender + ", receiver=" + receiver + ", message=" + message + "]";
    }   
}
