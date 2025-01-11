package com.quynhlm.dev.be.model.dto.responseDTO;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor

public class ComplaintResponseDTO {
    private Integer userId;
    private String email;
    private String avatar;
    private Integer complaintId;
    private String type;
    private String complaintReason;
    private String attachment;
    private String status;
    private String responseTime;
    private String responseMessage;
    private String report_reason;
    private String violation_type;
    private LocalDateTime lock_date;
}
