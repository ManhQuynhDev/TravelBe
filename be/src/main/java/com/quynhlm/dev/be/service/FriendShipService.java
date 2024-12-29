package com.quynhlm.dev.be.service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.quynhlm.dev.be.core.exception.GroupNotFoundException;
import com.quynhlm.dev.be.core.exception.MethodNotValidException;
import com.quynhlm.dev.be.core.exception.UnknownException;
import com.quynhlm.dev.be.core.exception.UserAccountNotFoundException;
import com.quynhlm.dev.be.core.exception.UserWasAlreadyRequest;
import com.quynhlm.dev.be.enums.FriendRequest;
import com.quynhlm.dev.be.model.dto.requestDTO.InviteRequestDTO;
import com.quynhlm.dev.be.model.dto.responseDTO.UserDTO;
import com.quynhlm.dev.be.model.dto.responseDTO.UserFriendResponse;
import com.quynhlm.dev.be.model.dto.responseDTO.UserFriendResponseDTO;
import com.quynhlm.dev.be.model.dto.responseDTO.UserTagPostResponse;
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
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class FriendShipService {

    @Autowired
    private FriendShipRepository friendShipRepository;

    @Autowired
    private UserRepository userRepository;

    private static final double EARTH_RADIUS = 6371.0;

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

        FriendShip foundFriendShip = friendShipRepository.findByUserIdsWithFixedStatuses(
                userSendId, userReceivedId);

        if (foundFriendShip != null) {
            throw new UserWasAlreadyRequest("Cannot send friend request because you are already friends.");
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

    public Page<UserFriendResponse> findAllResquestFriends(Integer userId, int page, int size) {
        User foundUser = userRepository.getAnUser(userId);
        if (foundUser == null) {
            throw new UserAccountNotFoundException(
                    "Find user send request with " + userId + " not found , please try again !");
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<Object[]> results = friendShipRepository.findAllRequestFriends(userId, pageable);

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

    public Page<UserFriendResponse> findByGetListFriends(Integer userId, int page, int size) {
        User foundUser = userRepository.getAnUser(userId);
        if (foundUser == null) {
            throw new UserAccountNotFoundException(
                    "Find user send request with " + userId + " not found , please try again !");
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<Object[]> results = friendShipRepository.getAllFriends(userId, pageable);

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

    public double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS * c;
    }

    public Page<UserDTO> mayBeYouKnow(Integer userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Object[]> suggestionFriendsOfFriends = friendShipRepository.suggestionFriendsOfFriends(userId,
                pageable);
        return suggestionFriendsOfFriends.map(row -> {
            UserDTO object = new UserDTO();
            object.setUserId(((Number) row[0]).intValue());
            object.setFullname(((String) row[1]));
            object.setAvatar((String) row[2]);
            object.setLatitude((String) row[3]);
            object.setLongitude((String) row[4]);
            return object;
        });
    }

    public Page<UserTagPostResponse> suggestionFriends(Integer userId, double maxDistanceKm, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        User foundUser = userRepository.getAnUser(userId);
        if (foundUser == null) {
            throw new UserAccountNotFoundException(
                    "User with ID " + userId + " not found. Please try again!");
        }

        if (foundUser.getLatitude() == null || foundUser.getLongitude() == null) {
            Page<Object[]> suggestionFriendsOfFriends = friendShipRepository.suggestionFriendsOfFriends(userId,
                    pageable);
            return suggestionFriendsOfFriends.map(row -> {
                UserTagPostResponse object = new UserTagPostResponse();
                object.setUserId(((Number) row[0]).intValue());
                object.setFullname(((String) row[1]));
                object.setAvatarUrl((String) row[2]);
                return object;
            });
        }

        List<Object[]> users = friendShipRepository.getUserArrowNotFriend(userId);

        List<UserDTO> listUser = new ArrayList<>();
        listUser = users.stream()
                .map(user -> {
                    UserDTO userDTO = new UserDTO();
                    userDTO.setUserId(((Number) user[0]).intValue());
                    userDTO.setFullname((String) user[1]);
                    userDTO.setAvatar((String) user[2]);
                    userDTO.setLatitude((String) user[3]);
                    userDTO.setLongitude((String) user[4]);
                    return userDTO;
                })
                .collect(Collectors.toList());

        List<UserDTO> filteredUsers = listUser.stream()
                .filter(user -> {
                    double distance = calculateDistance(
                            Double.parseDouble(foundUser.getLatitude()),
                            Double.parseDouble(foundUser.getLongitude()),
                            Double.parseDouble(user.getLatitude()),
                            Double.parseDouble(user.getLongitude()));
                    return distance <= maxDistanceKm;
                })
                .collect(Collectors.toList());

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), filteredUsers.size());
        List<UserDTO> paginatedUsers = filteredUsers.subList(start, end);

        List<UserTagPostResponse> response = paginatedUsers.stream()
                .map(user -> new UserTagPostResponse(user.getUserId(), user.getFullname(), user.getAvatar()))
                .collect(Collectors.toList());

        return new PageImpl<>(response, pageable, filteredUsers.size());
    }

    public void acceptFriend(int userSendId, int userReceivedId, String action)
            throws UserAccountNotFoundException, UnknownException, UserWasAlreadyRequest {

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

        FriendShip foundFriendShip = friendShipRepository.findByUserIdsWithFixedStatuses(userSendId,
                userReceivedId);

        if (foundFriendShip == null) {
            throw new UserWasAlreadyRequest(
                    "Transaction cannot be completed because userSendId and userReceivedId not status PENDING");
        }

        if ("approved".equalsIgnoreCase(action)) {
            foundFriendShip.setStatus("APPROVED");
            isSuccess(foundFriendShip);
        } else if ("reject".equalsIgnoreCase(action)) {
            friendShipRepository.delete(foundFriendShip);
        } else {
            throw new IllegalArgumentException("Invalid action");
        }
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

        FriendShip foundFriendShip = friendShipRepository.findByUserIdsWithFixedStatuses(userSendId,
                userReceivedId);

        if (foundFriendShip == null) {
            throw new UserWasAlreadyRequest(
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

        FriendShip foundFriendShip = friendShipRepository.findByUserIdsWithFixedStatuses(userSendId,
                userReceivedId);

        if (foundFriendShip == null) {
            throw new UserWasAlreadyRequest(
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
