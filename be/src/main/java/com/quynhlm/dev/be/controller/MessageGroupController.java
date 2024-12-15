package com.quynhlm.dev.be.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.quynhlm.dev.be.core.ResponseObject;
import com.quynhlm.dev.be.model.dto.requestDTO.MessageRequestDTO;
import com.quynhlm.dev.be.model.dto.responseDTO.UserMessageGroupResponseDTO;
import com.quynhlm.dev.be.model.dto.responseDTO.UserMessageResponseDTO;
import com.quynhlm.dev.be.model.entity.Message;
import com.quynhlm.dev.be.service.MessageGroupService;
import com.quynhlm.dev.be.service.MessageService;
import com.quynhlm.dev.be.service.MessageStatusService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;

@RestController
@Slf4j
@RequestMapping(path = "/api/messages")
public class MessageGroupController {

    @Autowired
    private MessageGroupService messageGroupService;

    @Autowired
    private MessageService messageService;

    @Autowired
    private MessageStatusService messageStatusService;

    @GetMapping("")
    public Page<UserMessageResponseDTO> getAllMessageUser(@RequestParam Integer senderId,
            @RequestParam Integer receiverId, Pageable pageable) {
        return messageService.getAllMessageUser(senderId, receiverId, pageable);
    }

    @GetMapping("/last-message/{user_id}")
    public Message getLastMessage(@PathVariable Integer user_id) {
        return messageService.getLastMessage(user_id);
    }

    @PostMapping("")
    public ResponseEntity<ResponseObject<UserMessageResponseDTO>> sendMessage(
            @RequestBody Message message) {
        ResponseObject<UserMessageResponseDTO> response = new ResponseObject<>();

        UserMessageResponseDTO result = messageService.sendMessage(message);
        response.setStatus(true);
        response.setMessage("Send Message Successfully");
        response.setData(result);
        return new ResponseEntity<ResponseObject<UserMessageResponseDTO>>(response, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResponseObject<Void>> updateMessage(@PathVariable Integer id, @RequestBody String content) {
        ResponseObject<Void> response = new ResponseObject<>();
        messageService.updateMessage(id, content);
        response.setStatus(true);
        response.setMessage("Update Message Successfully");
        return new ResponseEntity<ResponseObject<Void>>(response, HttpStatus.OK);
    }

    @PutMapping("/change_status/{id}")
    public ResponseEntity<ResponseObject<Void>> updateStatusMessage(@PathVariable Integer id) {
        ResponseObject<Void> response = new ResponseObject<>();

        messageService.changeStatusMessage(id);
        response.setStatus(true);
        response.setMessage("Update Status Message Successfully");
        return new ResponseEntity<ResponseObject<Void>>(response, HttpStatus.OK);
    }

    @DeleteMapping("/{messageId}")
    public ResponseEntity<ResponseObject<Void>> deleteMessage(
            @PathVariable Integer messageId) {
        ResponseObject<Void> response = new ResponseObject<>();
        messageService.deleteMessage(messageId);
        response.setStatus(true);
        response.setMessage("Delete message sucessfully");
        return new ResponseEntity<ResponseObject<Void>>(response, HttpStatus.OK);
    }

    @GetMapping("/group/{groupId}")
    public Page<UserMessageGroupResponseDTO> getAllListMessage(@PathVariable Integer groupId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "2") int size) {
        return messageGroupService.getAllListData(groupId, page, size);
    }

    @PostMapping("/group")
    public ResponseEntity<ResponseObject<UserMessageGroupResponseDTO>> sendMessageGroup(
            @RequestBody MessageRequestDTO messageRequestDTO) {
        ResponseObject<UserMessageGroupResponseDTO> response = new ResponseObject<>();

        UserMessageGroupResponseDTO result = messageGroupService.sendMessage(messageRequestDTO);
        response.setStatus(true);
        response.setMessage("Send Message Successfuly");
        response.setData(result);
        return new ResponseEntity<ResponseObject<UserMessageGroupResponseDTO>>(response, HttpStatus.OK);
    }

    @DeleteMapping("/group/{messageId}")
    public ResponseEntity<ResponseObject<Void>> deleteMessageGroup(
            @PathVariable Integer messageId) {
        ResponseObject<Void> response = new ResponseObject<>();
        messageGroupService.deleteMessage(messageId);
        response.setStatus(true);
        response.setMessage("Delete message sucessfully");
        return new ResponseEntity<ResponseObject<Void>>(response, HttpStatus.OK);
    }

    @PutMapping("/group")
    public ResponseEntity<ResponseObject<Void>> updateStatusMessageGroup(@RequestParam Integer viewId,
            @RequestParam Integer messageId, @RequestParam Boolean status) {
        ResponseObject<Void> response = new ResponseObject<>();
        messageStatusService.changeStatusMessage(viewId, messageId, status);
        response.setStatus(true);
        response.setMessage("Update Status Message Successfully");
        return new ResponseEntity<ResponseObject<Void>>(response, HttpStatus.OK);
    }
}
