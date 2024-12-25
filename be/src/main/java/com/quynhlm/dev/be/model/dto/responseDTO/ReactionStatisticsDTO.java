package com.quynhlm.dev.be.model.dto.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class ReactionStatisticsDTO {
    private int like;
    private int love;
    private int haha;
    private int wow;
    private int sad;
    private int angry;
}
