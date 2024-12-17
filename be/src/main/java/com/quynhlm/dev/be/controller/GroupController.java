package com.quynhlm.dev.be.controller;

import java.util.List;

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

    @GetMapping("")
    public Page<GroupResponseDTO> getAllListGroups(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "2") int size) {
        return groupService.getAllGroup(page, size);
    }

    @GetMapping("/top_member")
    public List<GroupResponseDTO> Top10GroupByMembers() {
        return groupService.Top10GroupByMembers();
    }

    @GetMapping("/top_travel_plan")
    public List<GroupResponseDTO> Top10GroupTravel() {
        return groupService.Top10GroupTravel();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseObject<GroupResponseDTO>> getAnGroup(@PathVariable Integer id) {
        ResponseObject<GroupResponseDTO> result = new ResponseObject<>();
        result.setMessage("Get group with " + id + "successfully");
        result.setStatus(true);
        result.setData(groupService.getAnGroupWithId(id));
        return new ResponseEntity<ResponseObject<GroupResponseDTO>>(result, HttpStatus.OK);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<GroupResponseDTO>> searchGroups(@RequestParam("q") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "2") int size) {
        Page<GroupResponseDTO> groups = groupService.searchGroupsByName(keyword, page, size);
        return ResponseEntity.ok(groups);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseObject<Void>> deleteGroup(@PathVariable Integer id) {
        ResponseObject<Void> result = new ResponseObject<>();
        groupService.deleteGroup(id);
        result.setMessage("Delete group with " + id + "successfully");
        result.setStatus(true);
        return new ResponseEntity<ResponseObject<Void>>(result, HttpStatus.OK);
    }

    @PostMapping("")
    public ResponseEntity<ResponseObject<Group>> insertGroup(@RequestPart("group") String groupJson,
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

    @PutMapping("/{id}")
    public ResponseEntity<ResponseObject<Void>> updateGroup(
            @PathVariable Integer id,
            @RequestPart("group") String groupJson,
            @RequestPart(value = "file", required = false) MultipartFile file) {

        ObjectMapper objectMapper = new ObjectMapper();
        SettingsGroupDTO group = null;
        try {
            group = objectMapper.readValue(groupJson, SettingsGroupDTO.class);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
           
        groupService.settingGroup(id, group, file);
        ResponseObject<Void> result = new ResponseObject<>();
        result.setMessage("Create a new group successfully");
        result.setStatus(true);
        return new ResponseEntity<ResponseObject<Void>>(result, HttpStatus.OK);
    }
}
