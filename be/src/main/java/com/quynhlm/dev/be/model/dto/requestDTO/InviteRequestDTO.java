package com.quynhlm.dev.be.model.dto.requestDTO;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor

public class InviteRequestDTO {
    private Integer userSendId;
    private Integer groupId;
    private List<Integer> friendIds;
}
