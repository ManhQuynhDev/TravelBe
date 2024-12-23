package com.quynhlm.dev.be.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.quynhlm.dev.be.core.ResponseObject;
import com.quynhlm.dev.be.model.dto.requestDTO.InviteRequestDTO;
import com.quynhlm.dev.be.model.dto.responseDTO.UserFriendResponse;
import com.quynhlm.dev.be.model.dto.responseDTO.UserFriendResponseDTO;
import com.quynhlm.dev.be.model.dto.responseDTO.UserTagPostResponse;
import com.quynhlm.dev.be.service.FriendShipService;
import com.quynhlm.dev.be.service.InvitationService;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api/friend")
@RequiredArgsConstructor

public class FriendShipController {
    @Autowired
    private FriendShipService friendShipService;

    @Autowired
    private InvitationService invitationService;

    @GetMapping("/user_friend")
    public Page<UserFriendResponseDTO> getAll(
            @RequestParam Integer userId,
            @RequestParam Integer groupId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "2") int size) {
        return friendShipService.getAllListUserFriend(userId, groupId, page, size);
    }
    @PostMapping("/send_invite")
    public ResponseEntity<ResponseObject<Void>> inviteFriends(@RequestBody InviteRequestDTO inviteRequestDTO) {
        ResponseObject<Void> result = new ResponseObject<>();
        friendShipService.inviteFriends(inviteRequestDTO);
        result.setMessage("Send request add friend successfully");
        result.setStatus(true);
        return new ResponseEntity<ResponseObject<Void>>(result, HttpStatus.OK);
    }

    @PutMapping("/accept_invitation")
    public ResponseEntity<ResponseObject<Void>> acceptInvitation(
            @RequestParam Integer userId,
            @RequestParam Integer groupId) {
        ResponseObject<Void> result = new ResponseObject<>();
        invitationService.acceptInvitation(userId, groupId);
        result.setMessage("Accept join group successfully");
        result.setStatus(true);
        return new ResponseEntity<ResponseObject<Void>>(result, HttpStatus.OK);
    }

    @GetMapping("/get-all-friends/{userId}")
    public Page<UserFriendResponse> getListRequestByStatus(@PathVariable Integer userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "2") int size) {
        return friendShipService.findByGetListFriends(userId, page, size);
    }

    @GetMapping("/get-all-request/{userId}")
    public Page<UserFriendResponse> getAllRequestFriends(@PathVariable Integer userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "2") int size) {
        return friendShipService.findAllResquestFriends(userId, page, size);
    }

    @GetMapping("/suggestions/{userId}")
    public Page<UserTagPostResponse> suggestionFriends(
            @PathVariable Integer userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "10") double maxDistanceKm) {
        return friendShipService.suggestionFriends(userId, maxDistanceKm, page, size);
    }   
    @PostMapping("/request-to-friend/{userSendId}/{userReceivedId}")
    public ResponseEntity<ResponseObject<Void>> sendRequestAddFriend(@PathVariable Integer userSendId,
            @PathVariable Integer userReceivedId) {
        ResponseObject<Void> result = new ResponseObject<>();
        friendShipService.sendingRequestFriend(userSendId, userReceivedId);
        result.setMessage("Send request add friend successfully");
        result.setStatus(true);
        return new ResponseEntity<ResponseObject<Void>>(result, HttpStatus.OK);
    }

    @PutMapping("/accept-friend/{userSendId}/{userReceivedId}/action")
    public ResponseEntity<ResponseObject<Void>> acceptFriend(@PathVariable Integer userSendId,
            @PathVariable Integer userReceivedId, @RequestParam String action) {
        ResponseObject<Void> result = new ResponseObject<>();
        friendShipService.acceptFriend(userSendId, userReceivedId, action);
        result.setMessage("Update accept friend successfully");
        result.setStatus(true);
        return new ResponseEntity<ResponseObject<Void>>(result, HttpStatus.OK);
    }

    @DeleteMapping("/cancel/{userSendId}/{userReceivedId}")
    public ResponseEntity<ResponseObject<Void>> cancelFriend(@PathVariable Integer userSendId,
            @PathVariable Integer userReceivedId) {
        ResponseObject<Void> result = new ResponseObject<>();
        friendShipService.cancelFriends(userSendId, userReceivedId);
        result.setMessage("Delete friend successfully");
        result.setStatus(true);
        return new ResponseEntity<ResponseObject<Void>>(result, HttpStatus.OK);
    }
    // ChangeStatus friends

    @PutMapping("/change-status/{userSendId}/{userReceivedId}/action")
    public ResponseEntity<ResponseObject<Void>> changeStatus(@PathVariable Integer userSendId,
            @PathVariable Integer userReceivedId, @RequestParam String action) {
        ResponseObject<Void> result = new ResponseObject<>();
        friendShipService.changeStatusFriend(userSendId, userReceivedId, action);
        result.setMessage("Update status friend successfully");
        result.setStatus(true);
        return new ResponseEntity<ResponseObject<Void>>(result, HttpStatus.OK);
    }
}
