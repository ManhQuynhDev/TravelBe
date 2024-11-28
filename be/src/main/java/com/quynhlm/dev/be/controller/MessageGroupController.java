package com.quynhlm.dev.be.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.quynhlm.dev.be.model.dto.responseDTO.UserMessageResponseDTO;
import com.quynhlm.dev.be.service.MessageGroupService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping(path = "/api/messages")
public class MessageGroupController {

    @Autowired
    private MessageGroupService messageGroupService;

    @GetMapping("/group-id/{groupId}")
    public Page<UserMessageResponseDTO> getAllListMessage(@PathVariable Integer groupId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "2") int size) {
        return messageGroupService.getAllListData(groupId, page, size);
    }
}
