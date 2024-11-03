package com.quynhlm.dev.be.service;

import java.sql.Timestamp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.quynhlm.dev.be.core.exception.UnknownException;
import com.quynhlm.dev.be.core.exception.UserAccountNotFoundException;
import com.quynhlm.dev.be.enums.FriendRequest;
import com.quynhlm.dev.be.model.entity.FriendShip;
import com.quynhlm.dev.be.model.entity.Notification;
import com.quynhlm.dev.be.model.entity.User;
import com.quynhlm.dev.be.repositories.FriendShipRepository;
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
    private NotificationService notificationService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

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

        FriendShip foundFriendShip = friendShipRepository.findByUserSendIdAndUserReceivedIdAndStatusIn(
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
        } else {
            pushNotification(friendShip.getUserSendId(), friendShip.getUserReceivedId(),
                    "đã gửi cho bạn một lời mời kết bạn", "Lời mời kết bạn từ ");
        }
    }

    public Page<FriendShip> findByUserReceivedIdAndStatus(Integer userReceivedId, String status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return friendShipRepository.findByUserReceivedIdAndStatus(userReceivedId, status, pageable);
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

        FriendShip foundFriendShip = friendShipRepository.findByUserSendIdAndUserReceivedIdAndStatusIn(userSendId,
                userReceivedId, "PENDING");

        if (foundFriendShip == null) {
            throw new UnknownException(
                    "Transaction cannot be completed because userSendId and userReceivedId not status PENDING");
        }

        if ("approve".equalsIgnoreCase(action)) {
            foundFriendShip.setStatus("APPROVED");
            pushNotification(foundFriendShip.getUserSendId(), foundFriendShip.getUserReceivedId(),
                    "đã chấp nhận lời mời kết bạn", "Lời mời kết bạn được chấp nhận từ ");
        } else if ("reject".equalsIgnoreCase(action)) {
            friendShipRepository.delete(foundFriendShip);
            pushNotification(foundFriendShip.getUserSendId(), foundFriendShip.getUserReceivedId(),
                    "đã từ chối lời mời kết bạn", "Lời mời kết bạn bị từ chối từ ");
        } else {
            throw new IllegalArgumentException("Invalid action");
        }

        isSuccess(foundFriendShip);
    }

    public void cancelFriends(int userSendId, int userReceivedId) throws UserAccountNotFoundException {
        FriendShip foundFriendShip = friendShipRepository.findByUserSendIdAndUserReceivedIdAndStatusIn(userSendId,
                userReceivedId, "APPROVED");

        if (foundFriendShip == null) {
            throw new UnknownException(
                    "Cannot complete transaction because there is no friendship between userSendId " + userSendId +
                            " and userReceivedId " + userReceivedId);
        }

        friendShipRepository.delete(foundFriendShip);

        pushNotification(foundFriendShip.getUserReceivedId(), foundFriendShip.getUserSendId(),
                "đã hủy kết bạn với bạn", "Kết bạn đã bị hủy từ ");
    }

    public void pushNotification(int userReceivedId, int userSendId, String message, String title) {
        User foundUser = userRepository.getAnUser(userSendId);

        Notification notification = notificationService.saveNotification(
                userReceivedId, title + " " + foundUser.getFullname(),
                foundUser.getFullname() + " " + message);
        messagingTemplate.convertAndSend("/topic/notification/" + userReceivedId, notification);
    }

}
