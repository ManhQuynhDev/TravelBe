package com.quynhlm.dev.be.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.quynhlm.dev.be.core.ResponseObject;
import com.quynhlm.dev.be.model.entity.PostReaction;
import com.quynhlm.dev.be.service.PostReactionService;

import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import jakarta.validation.Valid;

@RestController
@RequestMapping(path = "/api/postReaction")
public class PostReactionController {
    @Autowired
    private PostReactionService postReactionService;

    @PutMapping("")
    public ResponseEntity<ResponseObject<Void>> updateReaction(@RequestBody @Valid PostReaction postReaction) {
        ResponseObject<Void> result = new ResponseObject<>();
        postReactionService.updateReaction(postReaction);
        result.setMessage("Update reaction successfully");
        return new ResponseEntity<ResponseObject<Void>>(result, HttpStatus.OK);
    }
}
