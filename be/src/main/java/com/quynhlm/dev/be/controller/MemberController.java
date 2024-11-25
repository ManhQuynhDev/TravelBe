package com.quynhlm.dev.be.controller;

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
    public Page<GroupResponseDTO> getAllListGroups(
            @PathVariable Integer userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "2") int size) {
        return memberService.foundGroupUserCreate(userId, page, size);
    }

    // FoundGroup member send   
    @GetMapping("/group-join/{userId}")
    public Page<MemberJoinGroupResponseDTO> getGroupMemberJoin(
            @PathVariable Integer userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "2") int size) {
        return memberService.getGroupMemberJoin(userId, page, size);
    }
    
    // Get
    @GetMapping("/{groupId}/status")
    public Page<Member> getListUserByStatus(@PathVariable Integer groupId, @RequestParam String status,
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

    // Duyệt thành viên memberId = managerId
    @PutMapping("/browse")
    public ResponseEntity<ResponseObject<Void>> updateMemberStatus(
            @RequestParam(name = "groupId") Integer groupId,
            @RequestParam(name = "managerId") Integer memberId,
            @RequestParam(name = "action") String action,
            @RequestParam(name = "memberSendRequestId") Integer memberSendRequestId) {

        ResponseObject<Void> result = new ResponseObject<>();
        memberService.updateMemberStatus(groupId, memberId, memberSendRequestId, action);
        result.setMessage("Member has been " + action + "d.");
        result.setStatus(true);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}
