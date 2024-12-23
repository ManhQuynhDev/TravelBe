package com.quynhlm.dev.be.model.dto.responseDTO;

import lombok.Setter;
import lombok.Getter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor

public class MediaResponseDTO {
    private String mediaUrl;
    private String mediaType;
}
