package com.quynhlm.dev.be.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.quynhlm.dev.be.core.ResponseObject;
import com.quynhlm.dev.be.model.entity.ReplyReaction;
import com.quynhlm.dev.be.service.ReplyReactionService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "api/reply-reaction")
public class ReplyReactionController {
    @Autowired
    private ReplyReactionService replyReactionService;

    @PutMapping("")
    public ResponseEntity<ResponseObject<Void>> updateReaction(@RequestBody @Valid ReplyReaction replyReaction) {
        ResponseObject<Void> result = new ResponseObject<>();
        replyReactionService.updateReaction(replyReaction);
        result.setMessage("Update reaction successfully");
        result.setStatus(true);
        return new ResponseEntity<ResponseObject<Void>>(result, HttpStatus.OK);
    }
}
