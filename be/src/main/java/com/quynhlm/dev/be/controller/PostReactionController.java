package com.quynhlm.dev.be.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.quynhlm.dev.be.core.ResponseObject;
import com.quynhlm.dev.be.model.dto.responseDTO.ReactionStatisticsDTO;
import com.quynhlm.dev.be.model.dto.responseDTO.UserReactionDTO;
import com.quynhlm.dev.be.model.entity.PostReaction;
import com.quynhlm.dev.be.service.PostReactionService;

import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

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
        result.setStatus(true);
        return new ResponseEntity<ResponseObject<Void>>(result, HttpStatus.OK);
    }

    @GetMapping("")
    public Page<UserReactionDTO> getAllUserReactionWithType(
            @RequestParam Integer postId,
            @RequestParam String type, Pageable pageable) {
        return postReactionService.getAllUserReactionWithType(postId, type, pageable);
    }

    @GetMapping("/reaction_count")
    public ReactionStatisticsDTO getReactionTypeCount(
            @RequestParam Integer postId) {
        return postReactionService.getReactionTypeCount(postId);
    }
}
