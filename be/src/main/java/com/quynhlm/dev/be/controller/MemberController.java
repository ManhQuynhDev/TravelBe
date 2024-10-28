package com.quynhlm.dev.be.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import com.quynhlm.dev.be.core.ResponseObject;
import com.quynhlm.dev.be.service.MemberService;

public class MemberController {

    @Autowired
    private MemberService memberService;

    @PutMapping("/groups/{groupId}/members/{memberId}/{action}")
    public ResponseEntity<ResponseObject<Void>> updateMemberStatus(
            @PathVariable Integer groupId,
            @PathVariable Integer memberId,
            @PathVariable String action,
            @RequestHeader("user-role") String userRole) {

        ResponseObject<Void> result = new ResponseObject<>();
        memberService.updateMemberStatus(groupId, memberId, action, userRole);
        result.setMessage("Member has been " + action + "d.\"");
        return new ResponseEntity<ResponseObject<Void>>(result, HttpStatus.OK);
    }
}
