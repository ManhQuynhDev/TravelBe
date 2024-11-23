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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quynhlm.dev.be.core.ResponseObject;
import com.quynhlm.dev.be.model.dto.requestDTO.GroupRequestDTO;
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

    //Get all list group
    @GetMapping("")
    public Page<GroupResponseDTO> getAllListGroups(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "2") int size) {
        return groupService.getAllGroup(page, size);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseObject<Group>> getAnGroup(@PathVariable Integer id) {
        ResponseObject<Group> result = new ResponseObject<>();
        result.setMessage("Delete group with " + id + "successfully");
        result.setStatus(true);
        result.setData(groupService.getAnGroupWithId(id));
        return new ResponseEntity<ResponseObject<Group>>(result, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseObject<Void>> deleteGroup(@PathVariable Integer id) {
        ResponseObject<Void> result = new ResponseObject<>();
        groupService.deleteGroup(id);
        result.setMessage("Delete group with " + id + "successfully");
        result.setStatus(true);
        return new ResponseEntity<ResponseObject<Void>>(result, HttpStatus.OK);
    }

    //Post group
    @PostMapping("")
    public ResponseEntity<ResponseObject<Group>> insertReview(@RequestPart("group") String groupJson,
            @RequestPart(value = "file", required = false) MultipartFile file) throws Exception {

         ObjectMapper objectMapper = new ObjectMapper();
         GroupRequestDTO group = null;
        try {
            group = objectMapper.readValue(groupJson, GroupRequestDTO.class);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        Group groupResponse = groupService.createGroup(group, file);
        ResponseObject<Group> result = new ResponseObject<>();
        result.setMessage("Create a new group successfully");
        result.setData(groupResponse);
        result.setStatus(true);
        return new ResponseEntity<ResponseObject<Group>>(result, HttpStatus.OK);
    }

    //Update
    @PutMapping("/{id}")
    public ResponseEntity<ResponseObject<Void>> updateGroup(
            @PathVariable Integer id,
            @RequestPart("group") SettingsGroupDTO settings,
            @RequestPart(value = "file", required = false) MultipartFile file) {
        groupService.settingGroup(id, settings, file);
        ResponseObject<Void> result = new ResponseObject<>();
        result.setMessage("Create a new group successfully");
        result.setStatus(true);
        return new ResponseEntity<ResponseObject<Void>>(result, HttpStatus.OK);
    }
}
