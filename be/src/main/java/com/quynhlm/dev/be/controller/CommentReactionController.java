package com.quynhlm.dev.be.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.quynhlm.dev.be.core.ResponseObject;
import com.quynhlm.dev.be.model.dto.responseDTO.UserReactionDTO;
import com.quynhlm.dev.be.model.entity.CommentReaction;
import com.quynhlm.dev.be.service.CommentReactionService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "api/comment-reaction")
public class CommentReactionController {

    @Autowired
    private CommentReactionService commentReactionService;

    @PutMapping("")
    public ResponseEntity<ResponseObject<Void>> updateReaction(@RequestBody @Valid CommentReaction commentReaction) {
        ResponseObject<Void> result = new ResponseObject<>();
        commentReactionService.updateReaction(commentReaction);
        result.setMessage("Update reaction successfully");
        result.setStatus(true);
        return new ResponseEntity<ResponseObject<Void>>(result, HttpStatus.OK);
    }

    @GetMapping("")
    public Page<UserReactionDTO> getAllUserReactionWithType(
            @RequestParam Integer commentId,
            @RequestParam String type, Pageable pageable) {
        return commentReactionService.getAllUserReactionWithType(commentId, type, pageable);
    }
}
