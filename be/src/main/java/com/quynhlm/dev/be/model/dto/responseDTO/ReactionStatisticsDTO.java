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
    private Integer id;
    private int likeCount;
    private int loveCount;
    private int hahaCount;
    private int wowCount;
    private int sadCount;
    private int angryCount;
}
