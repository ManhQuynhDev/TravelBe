package com.quynhlm.dev.be.service;

import java.util.Arrays;
import java.util.Optional;
import java.sql.Timestamp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.quynhlm.dev.be.core.exception.GroupNotFoundException;
import com.quynhlm.dev.be.core.exception.MemberNotFoundException;
import com.quynhlm.dev.be.core.exception.UnknownException;
import com.quynhlm.dev.be.core.exception.UserAccountNotFoundException;
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

        if (!groupRepository.existsById(member.getGroup_id())) {
            throw new GroupNotFoundException("Group with ID " + member.getGroup_id() + " not found.");
        }

        if (!userRepository.existsById(member.getUser_id())) {
            throw new UserAccountNotFoundException("User with ID " + member.getUser_id() + " not found.");
        }

        Optional<Member> existingMember = memberRepository.findByUser_idAndGroup_idAndStatusIn(
                member.getUser_id(), member.getGroup_id(), Arrays.asList("PENDING", "APPROVED"));

        if (existingMember.isPresent()) {
            throw new UnknownException("User has already requested to join or is already a member.");
        }

        member.setStatus("PENDING");
        Member saveMember = memberRepository.save(member);

        if (saveMember.getId() == null) {
            throw new UnknownException("Transaction cannot be completed!");
        }
    }

    public void deleleMember(Integer id) throws MemberNotFoundException {
        Member foundMember = memberRepository.findMemberById(id);
        if (foundMember == null) {
            throw new GroupNotFoundException("Member with ID " + id + " not found, please try another ID.");
        }
        memberRepository.delete(foundMember);
    }

    public void updateMemberStatus(int groupId, int memberId, String action, String userRole) {
        if (!userRole.equals("ADMIN") && !userRole.equals("MANAGER")) {
            throw new SecurityException("You do not have permission to approve/reject members.");
        }

        // Tìm thành viên theo ID
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found"));

        if (member.getGroup_id() != groupId) {
            throw new IllegalArgumentException("Group ID does not match");
        }

        // Cập nhật trạng thái dựa trên action
        if ("approve".equalsIgnoreCase(action)) {
            member.setStatus("APPROVED");
        } else if ("reject".equalsIgnoreCase(action)) {
            member.setStatus("REJECTED");
        } else {
            throw new IllegalArgumentException("Invalid action");
        }

        Member updatMember = memberRepository.save(member);
        if (updatMember == null) {
            throw new UnknownException("Transaction cannot be completed!");
        }
    }
}
