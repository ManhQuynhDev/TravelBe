package com.quynhlm.dev.be.service;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.quynhlm.dev.be.core.exception.GroupNotFoundException;
import com.quynhlm.dev.be.core.exception.MemberNotFoundException;
import com.quynhlm.dev.be.core.exception.TravelPlanNotFoundException;
import com.quynhlm.dev.be.core.exception.UnknownException;
import com.quynhlm.dev.be.core.exception.UserAccountNotFoundException;
import com.quynhlm.dev.be.enums.Role;
import com.quynhlm.dev.be.model.entity.MemberPlan;
import com.quynhlm.dev.be.model.entity.Travel_Plan;
import com.quynhlm.dev.be.repositories.MemberPlanRepository;
import com.quynhlm.dev.be.repositories.TravelPlanRepository;
import com.quynhlm.dev.be.repositories.UserRepository;

@Service
public class MemberPlanService {

    @Autowired
    private MemberPlanRepository memberPlanRepository;

    @Autowired
    private TravelPlanRepository travelPlanRepository;

    @Autowired
    private UserRepository userRepository;

    public void requestToJoinPlan(MemberPlan member)
            throws GroupNotFoundException, MemberNotFoundException, UserAccountNotFoundException, UnknownException {

        member.setJoin_time(new Timestamp(System.currentTimeMillis()).toString());

        if (!travelPlanRepository.existsById(member.getPlanId())) {
            throw new GroupNotFoundException("Plan with ID " + member.getPlanId() + " not found.");
        }

        if (!userRepository.existsById(member.getUserId())) {
            throw new UserAccountNotFoundException("User with ID " + member.getUserId() + " not found.");
        }

        Optional<MemberPlan> existingMember = memberPlanRepository.findByUserIdAndPlanIdAndStatusIn(
                member.getUserId(), member.getPlanId(), Arrays.asList("PENDING", "APPROVED"));

        if (existingMember.isPresent()) {
            throw new UnknownException("User has already requested to join or is already a member.");
        }

        member.setStatus("PENDING"); // Waiting for request
        MemberPlan saveMember = memberPlanRepository.save(member);

        if (saveMember.getId() == null) {
            throw new UnknownException("Transaction cannot be completed!");
        }
    }

    public void setAdminPlan(MemberPlan member)
            throws TravelPlanNotFoundException, UserAccountNotFoundException,
            UnknownException {

        member.setJoin_time(new Timestamp(System.currentTimeMillis()).toString());

        member.setRole(Role.ADMIN.name()); // default admin

        if (!travelPlanRepository.existsById(member.getPlanId())) {
            throw new GroupNotFoundException("Plan with ID " + member.getPlanId() + " not found.");
        }

        if (!userRepository.existsById(member.getUserId())) {
            throw new UserAccountNotFoundException("User with ID " + member.getUserId() + " not found.");
        }

        Optional<MemberPlan> existingMember = memberPlanRepository.findByUserIdAndPlanIdAndStatusIn(
                member.getUserId(), member.getPlanId(), Arrays.asList("PENDING", "APPROVED"));

        if (existingMember.isPresent()) {
            throw new UnknownException("User has already requested to join or is already a member.");
        }

        member.setStatus("APPROVED");
        MemberPlan saveMember = memberPlanRepository.save(member);

        if (saveMember.getId() == null) {
            throw new UnknownException("Transaction cannot be completed!");
        }
    }

    // Get plan with status
    public Page<MemberPlan> getRequestToJoinPlans(Integer planId, String status, int page, int size)
            throws TravelPlanNotFoundException {
        Travel_Plan foundPlan = travelPlanRepository.getAnTravel_Plan(planId);
        if (foundPlan == null) {
            throw new TravelPlanNotFoundException(
                    "Found member with planId " + planId + " not found , please try again");
        }
        Pageable pageable = PageRequest.of(page, size);
        return memberPlanRepository.getRequestToJoinPlan(planId, status, pageable);
    }

    // Update status user join plan
    public void updateMemberStatus(int planId, int memberSendRequestId, int managerId, String action)
            throws TravelPlanNotFoundException, UserAccountNotFoundException, UnknownException {

        MemberPlan memberManager = memberPlanRepository.findById(managerId) // Find quyền user
                .orElseThrow(() -> new UserAccountNotFoundException("Member not found"));

        MemberPlan memberSendRequest = memberPlanRepository.findById(memberSendRequestId) // Find quyền user
                .orElseThrow(() -> new UserAccountNotFoundException("Member not found"));

        if (!memberManager.getRole().equals("ADMIN") && !memberManager.getRole().equals("MANAGER")) {
            throw new UnknownException("You do not have permission to approve/reject members.");
        }

        Travel_Plan travel_Plan = travelPlanRepository.getAnTravel_Plan(planId);
        if (travel_Plan == null) {
            throw new TravelPlanNotFoundException("Found travel with " + planId + " not found , please try again");
        }

        if (memberSendRequest.getPlanId() != planId) {
            throw new UnknownException("Group ID does not match");
        }

        if ("approve".equalsIgnoreCase(action)) {
            memberSendRequest.setStatus("APPROVED");
            memberSendRequest.setRole(Role.USER.name());
        } else if ("reject".equalsIgnoreCase(action)) {
            memberPlanRepository.delete(memberSendRequest);
            // memberSendRequest.setStatus("REJECTED");
        } else {
            throw new UnknownException("Invalid action");
        }
        MemberPlan saveMember = memberPlanRepository.save(memberSendRequest);

        if (saveMember.getId() == null) {
            throw new UnknownException("Transaction cannot be completed!");
        }
    }
}
