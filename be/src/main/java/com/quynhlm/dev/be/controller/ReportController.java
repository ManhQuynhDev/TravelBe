package com.quynhlm.dev.be.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.quynhlm.dev.be.core.ResponseObject;
import com.quynhlm.dev.be.model.entity.Report;
import com.quynhlm.dev.be.service.ReportService;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;

@RestController
@RequestMapping(path = "api/report")
public class ReportController {
    @Autowired
    private ReportService reportService;

    @PostMapping("")
    public ResponseEntity<ResponseObject<Void>> createReport(@RequestBody Report report) {
        ResponseObject<Void> result = new ResponseObject<>();
        reportService.createReport(report);
        result.setMessage("Create a new report successfully");
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @DeleteMapping("/{reportId}")
    public ResponseEntity<ResponseObject<Void>> deleteReport(@PathVariable Integer reportId) {
        ResponseObject<Void> result = new ResponseObject<>();
        reportService.deleteReport(reportId);
        result.setMessage("Delete report successfully");
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PutMapping("/{userId}/{reportId}/action")
    public ResponseEntity<ResponseObject<Void>> handleReport(@PathVariable Integer userId,
            @PathVariable Integer reportId,
            @RequestParam String action) {
        ResponseObject<Void> result = new ResponseObject<>();
        reportService.handleReport(userId,reportId,action);
        result.setMessage("Handel report successfully");
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}
