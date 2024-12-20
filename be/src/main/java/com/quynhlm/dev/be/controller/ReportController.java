package com.quynhlm.dev.be.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quynhlm.dev.be.core.ResponseObject;
import com.quynhlm.dev.be.model.dto.requestDTO.ReportRequestDTO;
import com.quynhlm.dev.be.model.dto.responseDTO.ReportResponseDTO;
import com.quynhlm.dev.be.service.ReportService;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;

@RestController
@RequestMapping(path = "api/report")
public class ReportController {
    @Autowired
    private ReportService reportService;

    @PostMapping("")
    public ResponseEntity<ResponseObject<ReportResponseDTO>> createReport(@RequestPart("report") String reportJson,
            @RequestPart(value = "file", required = false) MultipartFile file) {

        ObjectMapper objectMapper = new ObjectMapper();
        ReportRequestDTO report = null;
        try {
            report = objectMapper.readValue(reportJson, ReportRequestDTO.class);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        ResponseObject<ReportResponseDTO> result = new ResponseObject<>();
        ReportResponseDTO response = reportService.createReport(report, file);
        result.setMessage("Create a new report successfully");
        result.setData(response);
        result.setStatus(true);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @DeleteMapping("/{reportId}")
    public ResponseEntity<ResponseObject<Void>> deleteReport(@PathVariable Integer reportId) {
        ResponseObject<Void> result = new ResponseObject<>();
        reportService.deleteReport(reportId);
        result.setMessage("Delete report successfully");
        result.setStatus(true);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PutMapping("/{userId}/{reportId}/action")
    public ResponseEntity<ResponseObject<Void>> handleReport(@PathVariable Integer userId,
            @PathVariable Integer reportId,
            @RequestParam String violation,
            @RequestParam String action,
            @RequestParam String status) {
        ResponseObject<Void> result = new ResponseObject<>();
        reportService.handleReport(userId, reportId, violation, action, status);
        result.setMessage("Handel report successfully");
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @GetMapping("/user_create/{userId}")
    public Page<ReportResponseDTO> getAllReportUserCreate(@PathVariable Integer userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "2") int size) {
        return reportService.getAllReportUserCreate(userId, page, size);
    }

    @GetMapping("")
    public Page<ReportResponseDTO> getAllReport(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "2") int size) {
        return reportService.getAllReport(page, size);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseObject<ReportResponseDTO>> getAnReport(@PathVariable Integer id) {
        ResponseObject<ReportResponseDTO> result = new ResponseObject<>();
        result.setData(reportService.findReportById(id));
        result.setMessage("Get an report " + id + " successfully");
        result.setStatus(true);
        return new ResponseEntity<ResponseObject<ReportResponseDTO>>(result, HttpStatus.OK);
    }
}
