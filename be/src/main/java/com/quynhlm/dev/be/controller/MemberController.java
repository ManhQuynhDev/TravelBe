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

    @GetMapping("/{groupId}/status")
    public Page<Member> getListUserByStatus(@PathVariable Integer groupId, @RequestParam String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "2") int size) {
        return memberService.getRequestToJoinGroup(groupId, status, page, size);
    }

    @PostMapping("/request-join-group")
    public ResponseEntity<ResponseObject<Void>> requestToJoinGroup(@RequestBody Member member) {
        ResponseObject<Void> result = new ResponseObject<>();
        memberService.requestToJoinGroup(member);
        result.setMessage("Send request join group successfully");
        return new ResponseEntity<ResponseObject<Void>>(result, HttpStatus.OK);
    }

    @DeleteMapping("/{memberId}")
    public ResponseEntity<ResponseObject<Void>> deleteMember(@PathVariable Integer memberId) {
        ResponseObject<Void> result = new ResponseObject<>();
        memberService.deleleMember(memberId);
        result.setMessage("Delete member successfully");
        return new ResponseEntity<ResponseObject<Void>>(result, HttpStatus.OK);
    }

    @PutMapping("/{groupId}/members/{memberId}/{action}/{memberSendRequestId}")
    public ResponseEntity<ResponseObject<Void>> updateMemberStatus(
            @PathVariable Integer groupId,
            @PathVariable Integer memberId,
            @PathVariable String action,
            @PathVariable Integer memberSendRequestId) {

        ResponseObject<Void> result = new ResponseObject<>();
        memberService.updateMemberStatus(groupId, memberId, memberSendRequestId, action);
        result.setMessage("Member has been " + action + "d.");
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}