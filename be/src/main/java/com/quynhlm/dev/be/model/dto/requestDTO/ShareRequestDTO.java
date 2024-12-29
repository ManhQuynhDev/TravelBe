package com.quynhlm.dev.be.model.dto.requestDTO;
import java.util.List;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor

public class ShareRequestDTO {
    @NotNull(message = "Share by ID is required")
    private Integer shareById;

    @NotNull(message = "Post ID is required")
    private Integer postId;

    @Size(max = 200, message = "Share content must be less than 500 characters")
    private String content;

    @Size(max = 10, message = "Status must be less than 50 characters")
    private String status;

    private List<String> hashtags;

    @Override
    public String toString() {
        return "ShareRequestDTO [share_by_id=" + shareById + ", post_id=" + postId + ", shareContent=" + content
                + ", status=" + status + ", hashtags=" + hashtags + "]";
    }
}
