package com.quynhlm.dev.be.service;

import java.util.Arrays;
import java.util.Optional;
import java.sql.Timestamp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.quynhlm.dev.be.core.exception.GroupNotFoundException;
import com.quynhlm.dev.be.core.exception.MemberNotFoundException;
import com.quynhlm.dev.be.core.exception.UnknownException;
import com.quynhlm.dev.be.core.exception.UserAccountNotFoundException;
import com.quynhlm.dev.be.enums.Role;
import com.quynhlm.dev.be.model.entity.Group;
import com.quynhlm.dev.be.model.entity.Member;
import com.quynhlm.dev.be.repositories.GroupRepository;
import com.quynhlm.dev.be.repositories.MemberRepository;
import com.quynhlm.dev.be.repositories.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MemberService {
    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private UserRepository userRepository;

    public void requestToJoinGroup(Member member)
            throws GroupNotFoundException, MemberNotFoundException, UserAccountNotFoundException, UnknownException {

        member.setJoin_time(new Timestamp(System.currentTimeMillis()).toString());

        if (!groupRepository.existsById(member.getGroupId())) {
            throw new GroupNotFoundException("Group with ID " + member.getGroupId() + " not found.");
        }

        if (!userRepository.existsById(member.getUserId())) {
            throw new UserAccountNotFoundException("User with ID " + member.getUserId() + " not found.");
        }

        Optional<Member> existingMember = memberRepository.findByUser_idAndGroup_idAndStatusIn(
                member.getUserId(), member.getGroupId(), Arrays.asList("PENDING", "APPROVED"));

        if (existingMember.isPresent()) {
            throw new UnknownException("User has already requested to join or is already a member.");
        }

        member.setStatus("PENDING");
        Member saveMember = memberRepository.save(member);

        if (saveMember.getId() == null) {
            throw new UnknownException("Transaction cannot be completed!");
        }
    }

    public Page<Member> getRequestToJoinGroup(Integer groupId, String status, int page, int size)
            throws GroupNotFoundException {
        Group foundGroup = groupRepository.findGroupById(groupId);
        if (foundGroup == null) {
            throw new GroupNotFoundException("Found member with groupId " + groupId + " not found , please try again");
        }
        Pageable pageable = PageRequest.of(page, size);
        return memberRepository.getRequestToJoinGroup(groupId, status, pageable);
    }

    public void setAdminGroup(Member member)
            throws GroupNotFoundException, MemberNotFoundException, UserAccountNotFoundException, UnknownException {

        member.setJoin_time(new Timestamp(System.currentTimeMillis()).toString());

        if (!groupRepository.existsById(member.getGroupId())) {
            throw new GroupNotFoundException("Group with ID " + member.getGroupId() + " not found.");
        }

        if (!userRepository.existsById(member.getUserId())) {
            throw new UserAccountNotFoundException("User with ID " + member.getUserId() + " not found.");
        }

        Optional<Member> existingMember = memberRepository.findByUser_idAndGroup_idAndStatusIn(
                member.getUserId(), member.getGroupId(), Arrays.asList("PENDING", "APPROVED"));

        if (existingMember.isPresent()) {
            throw new UnknownException("User has already requested to join or is already a member.");
        }

        member.setStatus("APPROVED");
        Member saveMember = memberRepository.save(member);

        if (saveMember.getId() == null) {
            throw new UnknownException("Transaction cannot be completed!");
        }
    }

    public void deleleMember(Integer memberId) throws MemberNotFoundException {
        Member foundMember = memberRepository.findMemberById(memberId);
        if (foundMember == null) {
            throw new GroupNotFoundException("Member with ID " + memberId + " not found, please try another ID.");
        }
        memberRepository.delete(foundMember);
    }

    // member id == managerId
    public void updateMemberStatus(int groupId, int memberSendRequestId, int memberId, String action)
            throws UnknownException {

        Member memberManager = memberRepository.findById(memberId) // Find quyền user
                .orElseThrow(() -> new RuntimeException("Member not found"));

        Member memberSendRequest = memberRepository.findById(memberSendRequestId) // Find quyền user
                .orElseThrow(() -> new RuntimeException("Member not found"));

        if (!memberManager.getRole().equals("ADMIN") && !memberManager.getRole().equals("MANAGER")) {
            throw new UnknownException("You do not have permission to approve/reject members.");
        }

        if (memberSendRequest.getGroupId() != groupId) {
            throw new UnknownException("Group ID does not match");
        }

        if ("approve".equalsIgnoreCase(action)) {
            memberSendRequest.setStatus("APPROVED");
            memberSendRequest.setRole(Role.USER.name());
        } else if ("reject".equalsIgnoreCase(action)) {
            memberRepository.delete(memberSendRequest);
            // memberSendRequest.setStatus("REJECTED");
        } else {
            throw new UnknownException("Invalid action");
        }

        Member updatMember = memberRepository.save(memberSendRequest);
        if (updatMember == null) {
            throw new UnknownException("Transaction cannot be completed!");
        }
    }
}
