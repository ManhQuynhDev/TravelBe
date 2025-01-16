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
import com.quynhlm.dev.be.core.exception.UserWasAlreadyRequest;
import com.quynhlm.dev.be.enums.Role;
import com.quynhlm.dev.be.model.dto.responseDTO.MemberPlanResponseDTO;
import com.quynhlm.dev.be.model.entity.MemberPlan;
import com.quynhlm.dev.be.model.entity.Travel_Plan;
import com.quynhlm.dev.be.repositories.MemberPlanRepository;
import com.quynhlm.dev.be.repositories.TravelPlanRepository;
import com.quynhlm.dev.be.repositories.UserRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class MemberPlanService {

    @Autowired
    private MemberPlanRepository memberPlanRepository;

    @Autowired
    private TravelPlanRepository travelPlanRepository;

    @Autowired
    private UserRepository userRepository;

    public MemberPlan requestToJoinPlan(MemberPlan member)
            throws GroupNotFoundException, MemberNotFoundException, UserAccountNotFoundException,
            UnknownException {

        member.setJoin_time(new Timestamp(System.currentTimeMillis()).toString());

        member.setDelflag(0);

        if (!travelPlanRepository.existsById(member.getPlanId())) {
            throw new GroupNotFoundException("Plan with ID " + member.getPlanId() + " not found.");
        }

        if (!userRepository.existsById(member.getUserId())) {
            throw new UserAccountNotFoundException("User with ID " + member.getUserId() + " not found.");
        }

        member.setStatus("APPROVED");
        member.setRole(Role.USER.name());
        MemberPlan saveMember = memberPlanRepository.save(member);

        if (saveMember.getId() == null) {
            throw new UnknownException("Transaction cannot be completed!");
        }
        return saveMember;
    }

    public void deleteMemberPlan(Integer memberId, Integer planId) throws MemberNotFoundException {
        log.info("Member Id " + memberId + " " + "Plan id " + planId);
        MemberPlan foundMember = memberPlanRepository.findMemberById(memberId, planId);
        if (foundMember == null) {
            throw new MemberNotFoundException("Found user not found please try again");
        }
        foundMember.setDelflag(1);
        memberPlanRepository.save(foundMember);
    }

    public void setAdminPlan(MemberPlan member)
            throws TravelPlanNotFoundException, UserAccountNotFoundException,
            UnknownException, UserWasAlreadyRequest {

        member.setJoin_time(new Timestamp(System.currentTimeMillis()).toString());

        member.setRole(Role.ADMIN.name());

        if (!travelPlanRepository.existsById(member.getPlanId())) {
            throw new GroupNotFoundException("Plan with ID " + member.getPlanId() + " not found.");
        }

        if (!userRepository.existsById(member.getUserId())) {
            throw new UserAccountNotFoundException("User with ID " + member.getUserId() + " not found.");
        }

        Optional<MemberPlan> existingMember = memberPlanRepository.findByUserIdAndPlanIdAndStatusIn(
                member.getUserId(), member.getPlanId(), Arrays.asList("PENDING", "APPROVED"));

        if (existingMember.isPresent()) {
            throw new UserWasAlreadyRequest("User has already requested to join or is already a member.");
        }

        member.setStatus("APPROVED");
        MemberPlan saveMember = memberPlanRepository.save(member);

        if (saveMember.getId() == null) {
            throw new UnknownException("Transaction cannot be completed!");
        }
    }

    public Page<MemberPlanResponseDTO> getRequestToJoinPlans(Integer planId, int page, int size)
            throws TravelPlanNotFoundException {
        Travel_Plan foundPlan = travelPlanRepository.getAnTravel_Plan(planId);
        if (foundPlan == null) {
            throw new TravelPlanNotFoundException(
                    "Found member with planId " + planId + " not found , please try again");
        }
        Pageable pageable = PageRequest.of(page, size);

        Page<Object[]> results = memberPlanRepository.foundMemberJoinWithPlan(planId, pageable);

        return results.map(row -> {
            MemberPlanResponseDTO object = new MemberPlanResponseDTO();
            object.setUserId(((Number) row[0]).intValue());
            object.setPlanId(((Number) row[1]).intValue());
            object.setMemberId(((Number) row[2]).intValue());
            object.setFullname(((String) row[3]));
            object.setAvatar_url((String) row[4]);
            object.setRole((String) row[5]);
            object.setJoin_time((String) row[6]);
            return object;
        });
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
