package com.quynhlm.dev.be.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.quynhlm.dev.be.core.ResponseObject;
import com.quynhlm.dev.be.model.dto.requestDTO.SettingsGroupDTO;
import com.quynhlm.dev.be.model.dto.responseDTO.GroupResponseDTO;
import com.quynhlm.dev.be.model.entity.Group;
import com.quynhlm.dev.be.service.GroupService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/groups")
public class GroupController {

    @Autowired
    private final GroupService groupService;

    @GetMapping("")
    public Page<GroupResponseDTO> getAllListGroups(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "2") int size) {
        return groupService.getAllGroup(page, size);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseObject<Void>> deleteGroup(@PathVariable Integer id) {
        ResponseObject<Void> result = new ResponseObject<>();
        groupService.deleteGroup(id);
        result.setMessage("Delete group with " + id + "successfully");
        return new ResponseEntity<ResponseObject<Void>>(result, HttpStatus.OK);
    }

    @PostMapping("")
    public ResponseEntity<ResponseObject<Void>> insertReview(@RequestPart("group") Group group,
            @RequestPart(value = "file", required = false) MultipartFile file) throws Exception {
        groupService.createGroup(group, file);
        ResponseObject<Void> result = new ResponseObject<>();
        result.setMessage("Create a new group successfully");
        return new ResponseEntity<ResponseObject<Void>>(result, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResponseObject<Void>> updateGroup(
            @PathVariable Integer id,
            @RequestPart("group") SettingsGroupDTO settings,
            @RequestPart(value = "file", required = false) MultipartFile file) {
        ResponseObject<Void> result = new ResponseObject<>();
        result.setMessage("Create a new group successfully");
        groupService.settingGroup(id, settings, file);
        return new ResponseEntity<ResponseObject<Void>>(result, HttpStatus.OK);
    }
}
