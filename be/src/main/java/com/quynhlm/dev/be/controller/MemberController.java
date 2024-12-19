package com.quynhlm.dev.be.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.quynhlm.dev.be.core.ResponseObject;
import com.quynhlm.dev.be.model.dto.responseDTO.GroupResponseDTO;
import com.quynhlm.dev.be.model.dto.responseDTO.MemberJoinGroupResponseDTO;
import com.quynhlm.dev.be.model.dto.responseDTO.MemberResponseDTO;
import com.quynhlm.dev.be.model.entity.Member;
import com.quynhlm.dev.be.service.MemberService;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RequestMapping(path = "/api/member")
@RestController
@RequiredArgsConstructor
public class MemberController {

    @Autowired
    private MemberService memberService;

    // Found memment join groups
    @GetMapping("/member-join-group/{groupId}")
    public Page<MemberResponseDTO> foundMemberJoinGroup(
            @PathVariable Integer groupId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "2") int size) {
        return memberService.getListMemberFromGroup(groupId, page, size);
    }

    // There are groups user create
    @GetMapping("/user-create/{userId}")
    public Page<GroupResponseDTO> getAllListGroupUserCreate(
            @PathVariable Integer userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "2") int size) {
        return memberService.foundGroupUserCreate(userId, page, size);
    }

    @GetMapping("/user-create-id/{userId}")
    public ResponseEntity<ResponseObject<List<Integer>>> getAllListGroupIdUserCreate(
            @PathVariable Integer userId) {
        ResponseObject<List<Integer>> result = new ResponseObject<>();
        result.setData(memberService.fetchGroupIdUserCreate(userId));
        result.setStatus(true);
        result.setMessage("Get all group Id user create group successfully");
        return new ResponseEntity<ResponseObject<List<Integer>>>(result, HttpStatus.OK);
    }

    @GetMapping("/group-join-id/{userId}")
    public ResponseEntity<ResponseObject<List<Integer>>> foundUserJoinGroupId(
            @PathVariable Integer userId) {
        ResponseObject<List<Integer>> result = new ResponseObject<>();
        result.setData(memberService.foundUserJoinGroupId(userId));
        result.setStatus(true);
        result.setMessage("Get all group Id user join group successfully");
        return new ResponseEntity<ResponseObject<List<Integer>>>(result, HttpStatus.OK);
    }

    @GetMapping("/user-create/search")
    public Page<GroupResponseDTO> searchGroupUserCreate(
            @RequestParam Integer user_id,
            @RequestParam("q") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "2") int size) {
        return memberService.searchGroupUserCreate(user_id, keyword, page, size);
    }

    // FoundGroup member send
    @GetMapping("/group-join/{userId}")
    public Page<MemberJoinGroupResponseDTO> getGroupMemberJoin(
            @PathVariable Integer userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "2") int size) {
        return memberService.getGroupMemberJoin(userId, page, size);
    }
    
    // Search group user join
    @GetMapping("/group-join/search")
    public ResponseEntity<Page<MemberJoinGroupResponseDTO>> searchGroups(@RequestParam Integer user_id,
            @RequestParam("q") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "2") int size) {
        Page<MemberJoinGroupResponseDTO> groups = memberService.searchGroupMemberJoin(user_id, keyword, page, size);
        return ResponseEntity.ok(groups);
    }
    // Get
    @GetMapping("/{groupId}/status")
    public Page<MemberResponseDTO> getListUserByStatus(@PathVariable Integer groupId,
            @RequestParam("status") String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "2") int size) {
        return memberService.getRequestToJoinGroup(groupId, status, page, size);
    }
    // request - join - group
    @PostMapping("/request-join-group")
    public ResponseEntity<ResponseObject<Void>> requestToJoinGroup(@RequestBody Member member) {
        ResponseObject<Void> result = new ResponseObject<>();
        memberService.requestToJoinGroup(member);
        result.setStatus(true);
        result.setMessage("Send request join group successfully");
        return new ResponseEntity<ResponseObject<Void>>(result, HttpStatus.OK);
    }

    // Delete member from group
    @DeleteMapping("/{memberId}")
    public ResponseEntity<ResponseObject<Void>> deleteMember(@PathVariable Integer memberId) {
        ResponseObject<Void> result = new ResponseObject<>();
        memberService.deleleMember(memberId);
        result.setStatus(true);
        result.setMessage("Delete member successfully");
        return new ResponseEntity<ResponseObject<Void>>(result, HttpStatus.OK);
    }

    @DeleteMapping("/reject_member")
    public ResponseEntity<ResponseObject<Void>> rejectMember(
            @RequestParam Integer groupId,
            @RequestParam Integer adminId,
            @RequestParam Integer memberSendId) {

        ResponseObject<Void> result = new ResponseObject<>();
        memberService.rejectMember(groupId, adminId, memberSendId);
        result.setMessage("Reject member success fully");
        result.setStatus(true);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PutMapping("/browse")
    public ResponseEntity<ResponseObject<Void>> updateMemberStatus(
            @RequestParam Integer groupId,
            @RequestParam Integer adminId,
            @RequestParam String action,
            @RequestParam Integer memberSendId) {

        ResponseObject<Void> result = new ResponseObject<>();
        memberService.updateMemberStatus(groupId, adminId, memberSendId, action);
        result.setMessage("Member has been " + action + "d.");
        result.setStatus(true);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PutMapping("/change-admin")
    public ResponseEntity<ResponseObject<Void>> changeRoleAdmin(
            @RequestParam Integer adminId,
            @RequestParam Integer memberId,
            @RequestParam Integer groupId) {

        ResponseObject<Void> result = new ResponseObject<>();
        memberService.changeRoleAdmin(adminId, memberId, groupId);
        result.setMessage("Change role admin successfully");
        result.setStatus(true);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}
