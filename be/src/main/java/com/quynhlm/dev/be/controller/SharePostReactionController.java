package com.quynhlm.dev.be.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.quynhlm.dev.be.core.ResponseObject;
import com.quynhlm.dev.be.model.entity.SharePostReaction;
import com.quynhlm.dev.be.service.SharePostReactionService;

import jakarta.validation.Valid;

@RestController
@RequestMapping(path = "api/share-reaction")
public class SharePostReactionController {
    @Autowired
    private SharePostReactionService sharePostReactionService;

    @PutMapping("")
    public ResponseEntity<ResponseObject<?>> updateReaction(@RequestBody @Valid SharePostReaction sharePostReaction) {
        ResponseObject<Boolean> result = new ResponseObject<>();
        sharePostReactionService.updateReaction(sharePostReaction);
        result.setMessage("Update reaction successfully");
        return new ResponseEntity<ResponseObject<?>>(result, HttpStatus.OK);
    }
}
