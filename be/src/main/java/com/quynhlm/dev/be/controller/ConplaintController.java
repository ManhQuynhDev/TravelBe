package com.quynhlm.dev.be.controller;

import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quynhlm.dev.be.core.ResponseObject;
import com.quynhlm.dev.be.core.exception.UnknownException;
import com.quynhlm.dev.be.core.exception.UserAccountNotFoundException;
import com.quynhlm.dev.be.model.dto.requestDTO.ComplaintResquestDTO;
import com.quynhlm.dev.be.model.dto.requestDTO.ComplaintUpdateDTO;
import com.quynhlm.dev.be.model.dto.responseDTO.ComplaintResponseDTO;
import com.quynhlm.dev.be.model.entity.Complaint;
import com.quynhlm.dev.be.service.ComplaintService;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping(path = "api/complaint")
public class ConplaintController {
    @Autowired
    private ComplaintService complaintService;

    @PostMapping(value = "", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResponseObject<?>> insertComplaint(
            @RequestPart("complaint") String json,
            @RequestPart(value = "file", required = false) MultipartFile imageFile)
            throws UnknownException, BadRequestException, UserAccountNotFoundException {

        ObjectMapper objectMapper = new ObjectMapper();
        ComplaintResquestDTO complaintDTO = null;
        try {
            complaintDTO = objectMapper.readValue(json, ComplaintResquestDTO.class);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        complaintService.createFromComphaint(complaintDTO, imageFile);
        ResponseObject<Void> result = new ResponseObject<>();
        result.setMessage("Create a new complaint successfully");
        result.setStatus(true);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResponseObject<?>> updateComplaint(@PathVariable Integer id,
            @RequestBody ComplaintUpdateDTO updateDTO) {
        complaintService.responseComplaint(id, updateDTO);
        ResponseObject<Void> result = new ResponseObject<>();
        result.setMessage("Update complaint successfully");
        result.setStatus(true);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @GetMapping("/history")
    public Page<Complaint> getUsers(
            @RequestParam() String email,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return complaintService.foundComplaintUserCreate(email, page, size);
    }

    @GetMapping("")
    public Page<ComplaintResponseDTO> getAllComplaint(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return complaintService.getAllComplaint(page, size);
    }
}
