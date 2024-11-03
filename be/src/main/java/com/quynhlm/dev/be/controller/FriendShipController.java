package com.quynhlm.dev.be.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.quynhlm.dev.be.core.ResponseObject;
import com.quynhlm.dev.be.model.entity.FriendShip;
import com.quynhlm.dev.be.service.FriendShipService;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;

@RestController
@RequestMapping("/api/friend")
@RequiredArgsConstructor

public class FriendShipController {
    @Autowired
    private FriendShipService friendShipService;

    @GetMapping("/{userReceivedId}/status")
    public Page<FriendShip> getListRequestByStatus(@PathVariable Integer userReceivedId, @RequestParam String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "2") int size) {
        return friendShipService.findByUserReceivedIdAndStatus(userReceivedId, status, page, size);
    }

    @PostMapping("/request-to-friend/{userSendId}/{userReceivedId}")
    public ResponseEntity<ResponseObject<Void>> sendRequestAddFriend(@PathVariable Integer userSendId,
            @PathVariable Integer userReceivedId) {

        ResponseObject<Void> result = new ResponseObject<>();
        friendShipService.sendingRequestFriend(userSendId, userReceivedId);
        result.setMessage("Send request add friend successfully");
        return new ResponseEntity<ResponseObject<Void>>(result, HttpStatus.OK);
    }

    @PutMapping("/accept-friend/{userSendId}/{userReceivedId}/action")
    public ResponseEntity<ResponseObject<Void>> acceptFriend(@PathVariable Integer userSendId,
            @PathVariable Integer userReceivedId, @RequestParam String action) {
        ResponseObject<Void> result = new ResponseObject<>();
        friendShipService.acceptFriend(userSendId, userReceivedId, action);
        result.setMessage("Update accept friend successfully");
        return new ResponseEntity<ResponseObject<Void>>(result, HttpStatus.OK);
    }

    @DeleteMapping("/cancel")
    public ResponseEntity<String> cancelFriend(@RequestParam int userSendId, @RequestParam int userReceivedId) {
        friendShipService.cancelFriends(userSendId, userReceivedId);
        return ResponseEntity.ok("Friendship canceled successfully.");
    }
}
