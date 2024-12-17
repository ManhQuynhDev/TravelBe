package com.quynhlm.dev.be.service;

import java.sql.Timestamp;
import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.quynhlm.dev.be.core.exception.GroupNotFoundException;
import com.quynhlm.dev.be.core.exception.MethodNotValidException;
import com.quynhlm.dev.be.core.exception.UnknownException;
import com.quynhlm.dev.be.core.exception.UserAccountNotFoundException;
import com.quynhlm.dev.be.enums.FriendRequest;
import com.quynhlm.dev.be.model.dto.requestDTO.InviteRequestDTO;
import com.quynhlm.dev.be.model.dto.responseDTO.UserFriendResponse;
import com.quynhlm.dev.be.model.dto.responseDTO.UserFriendResponseDTO;
import com.quynhlm.dev.be.model.entity.FriendShip;
import com.quynhlm.dev.be.model.entity.Group;
import com.quynhlm.dev.be.model.entity.Invitation;
import com.quynhlm.dev.be.model.entity.Member;
import com.quynhlm.dev.be.model.entity.User;
import com.quynhlm.dev.be.repositories.FriendShipRepository;
import com.quynhlm.dev.be.repositories.GroupRepository;
import com.quynhlm.dev.be.repositories.InvitationRepository;
import com.quynhlm.dev.be.repositories.MemberRepository;
import com.quynhlm.dev.be.repositories.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FriendShipService {

    @Autowired
    private FriendShipRepository friendShipRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private InvitationRepository invitationRepository;

    public Page<UserFriendResponseDTO> getAllListUserFriend(int user_id, int groupId, int page, int size)
            throws GroupNotFoundException {

        Group foundGroup = groupRepository.findGroupById(groupId);
        if (foundGroup == null) {
            throw new GroupNotFoundException("Found group with " + groupId + " not found , please try again");
        }

        Pageable pageable = PageRequest.of(page, size);

        Page<Object[]> results = friendShipRepository.getAllListUserFriends(user_id, pageable);

        return results.map(row -> {
            UserFriendResponseDTO object = new UserFriendResponseDTO();
            object.setUserId(((Number) row[0]).intValue());
            object.setFullname(((String) row[1]));
            object.setAvatarUrl((String) row[2]);

            Member member = memberRepository.foundUserMemberFriend(((Number) row[0]).intValue(), groupId);

            if (member == null) {
                object.setJoiner(false);
            } else {
                object.setJoiner(true);
            }

            return object;
        });
    }

    public void inviteFriends(InviteRequestDTO invitation) throws GroupNotFoundException, UserAccountNotFoundException {
        Group foundGroup = groupRepository.findGroupById(invitation.getGroupId());
        if (foundGroup == null) {
            throw new GroupNotFoundException(
                    "Found group with " + invitation.getGroupId() + " not found , please try again");
        }

        for (Integer userId : invitation.getFriendIds()) {
            User foundUser = userRepository.getAnUser(userId);
            if (foundUser != null) {
                Invitation newInvitation = new Invitation();
                newInvitation.setUserSendId(invitation.getUserSendId());
                newInvitation.setUserReceivedId(userId);
                newInvitation.setGroup_id(invitation.getGroupId());
                newInvitation.setStatus("PENDING");
                newInvitation.setCreate_time(new Timestamp(System.currentTimeMillis()).toString());
                invitationRepository.save(newInvitation);
            }
        }
    }

    public void sendingRequestFriend(int userSendId, int userReceivedId)
            throws UserAccountNotFoundException, UnknownException {
        User userSending = userRepository.getAnUser(userSendId);
        if (userSending == null) {
            throw new UserAccountNotFoundException(
                    "Find user send request with " + userSendId + " not found , please try again !");
        }

        User userReceived = userRepository.getAnUser(userReceivedId);
        if (userReceived == null) {
            throw new UserAccountNotFoundException(
                    "Find user received with " + userReceivedId + " not found , please try again !");
        }

        FriendShip foundFriendShip = friendShipRepository.findByUserIdsAndStatus(
                userSendId, userReceivedId, "APPROVED");

        if (foundFriendShip != null) {
            throw new UnknownException("Cannot send friend request because you are already friends.");
        }

        FriendShip friendShip = new FriendShip();
        friendShip.setCreate_time(new Timestamp(System.currentTimeMillis()).toString());
        friendShip.setStatus(FriendRequest.PENDING.name());
        friendShip.setUserSendId(userSendId);
        friendShip.setUserReceivedId(userReceivedId);
        isSuccess(friendShip);
    }

    public void isSuccess(FriendShip friendShip) throws UnknownException {
        FriendShip saveFriendShip = friendShipRepository.save(friendShip);
        if (saveFriendShip.getId() == null) {
            throw new UnknownException("Transaction cannot be completed!");
        }
    }

    public Page<UserFriendResponse> findByGetListFriends(Integer userId, String status, int page, int size) {
        User foundUser = userRepository.getAnUser(userId);
        if (foundUser == null) {
            throw new UserAccountNotFoundException(
                    "Find user send request with " + userId + " not found , please try again !");
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<Object[]> results = friendShipRepository.findByUserFriends(userId,status, pageable);

        return results.map(row -> {
            UserFriendResponse object = new UserFriendResponse();
            object.setUserId(((Number) row[0]).intValue());
            object.setFullname(((String) row[1]));
            object.setAvatarUrl((String) row[2]);
            object.setStatus(((String) row[3]));
            object.setSend_time((String) row[4]);
            return object;
        });
    }

    public void acceptFriend(int userSendId, int userReceivedId, String action)
            throws UserAccountNotFoundException, UnknownException {

        User userSending = userRepository.getAnUser(userSendId);
        if (userSending == null) {
            throw new UserAccountNotFoundException(
                    "Find user send request with " + userSendId + " not found , please try again !");
        }

        User userReceived = userRepository.getAnUser(userReceivedId);
        if (userReceived == null) {
            throw new UserAccountNotFoundException(
                    "Find user received with " + userReceivedId + " not found , please try again !");
        }

        FriendShip foundFriendShip = friendShipRepository.findByUserIdsAndStatus(userSendId,
                userReceivedId, "PENDING");

        if (foundFriendShip == null) {
            throw new UnknownException(
                    "Transaction cannot be completed because userSendId and userReceivedId not status PENDING");
        }

        if ("approved".equalsIgnoreCase(action)) {
            foundFriendShip.setStatus("APPROVED");
        } else if ("reject".equalsIgnoreCase(action)) {
            friendShipRepository.delete(foundFriendShip);
        } else {
            throw new IllegalArgumentException("Invalid action");
        }
        isSuccess(foundFriendShip);
    }

    public void cancelFriends(int userSendId, int userReceivedId) throws UserAccountNotFoundException {

        User userSending = userRepository.getAnUser(userSendId);
        if (userSending == null) {
            throw new UserAccountNotFoundException(
                    "Find user send request with " + userSendId + " not found , please try again !");
        }

        User userReceived = userRepository.getAnUser(userReceivedId);
        if (userReceived == null) {
            throw new UserAccountNotFoundException(
                    "Find user received with " + userReceivedId + " not found , please try again !");
        }

        FriendShip foundFriendShip = friendShipRepository.findByUserIdsAndStatus(userSendId,
                userReceivedId, "APPROVED");

        if (foundFriendShip == null) {
            throw new UnknownException(
                    "Cannot complete transaction because there is no friendship between userSendId " + userSendId +
                            " and userReceivedId " + userReceivedId);
        }

        friendShipRepository.delete(foundFriendShip);
    }

    public void changeStatusFriend(int userSendId, int userReceivedId, String action)
            throws UserAccountNotFoundException, UnknownException {

        User userSending = userRepository.getAnUser(userSendId);
        if (userSending == null) {
            throw new UserAccountNotFoundException(
                    "Find user send request with " + userSendId + " not found , please try again !");
        }

        User userReceived = userRepository.getAnUser(userReceivedId);
        if (userReceived == null) {
            throw new UserAccountNotFoundException(
                    "Find user received with " + userReceivedId + " not found , please try again !");
        }

        FriendShip foundFriendShip = friendShipRepository.findByUserIdsAndStatus(userSendId,
                userReceivedId, "APPROVED");

        if (foundFriendShip == null) {
            throw new UnknownException(
                    "Cannot complete transaction because there is no friendship between userSendId " + userSendId +
                            " and userReceivedId " + userReceivedId);
        }

        String[] statusUser = { "Blocked" };

        Boolean isCheck = action == null || Arrays.asList(statusUser).contains(action);

        if (isCheck == false) {
            throw new MethodNotValidException("Invalid status user type. Please try again !");
        }

        foundFriendShip.setStatus(action);
    }
}
