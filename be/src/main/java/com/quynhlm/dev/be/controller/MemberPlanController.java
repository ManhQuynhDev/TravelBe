package com.quynhlm.dev.be.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.quynhlm.dev.be.core.ResponseObject;
import com.quynhlm.dev.be.model.entity.MemberPlan;
import com.quynhlm.dev.be.service.MemberPlanService;

@RestController
@RequestMapping(name = "api/v1/member-plan")
public class MemberPlanController {
    @Autowired
    private MemberPlanService memberPlanService;

    @GetMapping("/{groupId}/status")
    public Page<MemberPlan> getListUserByStatus(@PathVariable Integer planId, @RequestParam String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "2") int size) {
        return memberPlanService.getRequestToJoinPlans(planId, status, page, size);
    }

    @PostMapping("/request-join-group")
    public ResponseEntity<ResponseObject<Void>> requestToJoinGroup(@RequestBody MemberPlan member) {
        ResponseObject<Void> result = new ResponseObject<>();
        memberPlanService.requestToJoinPlan(member);
        result.setMessage("Send request join group successfully");
        return new ResponseEntity<ResponseObject<Void>>(result, HttpStatus.OK);
    }

    @PutMapping("/browse")
    public ResponseEntity<ResponseObject<Void>> updateMemberStatus(
            @RequestParam(name = "planId") Integer planId,
            @RequestParam(name = "managerId") Integer memberId,
            @RequestParam(name = "action") String action,
            @RequestParam(name = "memberSendRequestId") Integer memberSendRequestId) {

        ResponseObject<Void> result = new ResponseObject<>();
        memberPlanService.updateMemberStatus(planId, memberId, memberSendRequestId, action);
        result.setMessage("Member has been " + action + "d.");
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}
