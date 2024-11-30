package com.quynhlm.dev.be.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.quynhlm.dev.be.core.ResponseObject;
import com.quynhlm.dev.be.model.entity.MemberPlan;
import com.quynhlm.dev.be.service.MemberPlanService;

@RestController
@RequestMapping("api/member-plan")
public class MemberPlanController {
    @Autowired
    private MemberPlanService memberPlanService;

    @GetMapping("/{planId}/status")
    public Page<MemberPlan> getListUserByStatus(@PathVariable Integer planId, @RequestParam String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "2") int size) {
        return memberPlanService.getRequestToJoinPlans(planId, status, page, size);
    }

    @DeleteMapping("/remove_member")
    public ResponseEntity<ResponseObject<Void>> deleteMemberPlan(@RequestParam Integer userId  , @RequestParam Integer planId) {
        ResponseObject<Void> result = new ResponseObject<>();
        memberPlanService.deleteMemberPlan(userId , planId);
        result.setStatus(true);
        result.setMessage("Delete member plan successfully");
        return new ResponseEntity<ResponseObject<Void>>(result, HttpStatus.OK);
    }


    @PostMapping("/request-join-plan")      
    public ResponseEntity<ResponseObject<MemberPlan>> requestToJoinPlan(@RequestBody MemberPlan member) {
        ResponseObject<MemberPlan> result = new ResponseObject<>();
        MemberPlan memberResponse = memberPlanService.requestToJoinPlan(member);
        result.setMessage("Send request join plan successfully");
        result.setData(memberResponse);
        result.setStatus(true);
        return new ResponseEntity<ResponseObject<MemberPlan>>(result, HttpStatus.OK);
    }

    // @PutMapping("/browse")
    // public ResponseEntity<ResponseObject<Void>> updateMemberStatus(
    //         @RequestParam(name = "planId") Integer planId,
    //         @RequestParam(name = "managerId") Integer memberId,
    //         @RequestParam(name = "action") String action,
    //         @RequestParam(name = "memberSendRequestId") Integer memberSendRequestId) {

    //     ResponseObject<Void> result = new ResponseObject<>();
    //     memberPlanService.updateMemberStatus(planId, memberId, memberSendRequestId, action);
    //     result.setMessage("Member has been " + action + "d.");
    //     return new ResponseEntity<>(result, HttpStatus.OK);
    // }
}
