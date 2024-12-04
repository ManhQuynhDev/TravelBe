package com.quynhlm.dev.be.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.quynhlm.dev.be.core.exception.GroupNotFoundException;
import com.quynhlm.dev.be.core.exception.UserAccountNotFoundException;
import com.quynhlm.dev.be.model.entity.Group;
import com.quynhlm.dev.be.model.entity.Invitation;
import com.quynhlm.dev.be.model.entity.Member;
import com.quynhlm.dev.be.model.entity.User;
import com.quynhlm.dev.be.repositories.GroupRepository;
import com.quynhlm.dev.be.repositories.InvitationRepository;
import com.quynhlm.dev.be.repositories.UserRepository;

@Service
public class InvitationService {
    @Autowired
    private InvitationRepository invitationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private MemberService memberService;

    public void acceptInvitation(Integer user_id, Integer groupId)
            throws GroupNotFoundException, UserAccountNotFoundException {
        User foundUser = userRepository.getAnUser(user_id);
        if (foundUser == null) {
            throw new UserAccountNotFoundException("found user with " + user_id + " not found please try again");
        }

        Group foundGroup = groupRepository.findGroupById(groupId);
        if (foundGroup == null) {
            throw new GroupNotFoundException(
                    "Found group with " + groupId + " not found , please try again");
        }

        Invitation foundInvitation = invitationRepository.findInvitationById(user_id, groupId);

        if (foundInvitation != null) {
            Member member = new Member();
            member.setGroupId(groupId);
            member.setUserId(user_id);
            memberService.insertMember(member);

            invitationRepository.delete(foundInvitation);
        }
    }
}
