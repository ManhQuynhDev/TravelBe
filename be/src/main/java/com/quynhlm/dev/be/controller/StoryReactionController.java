package com.quynhlm.dev.be.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.quynhlm.dev.be.core.ResponseObject;
import com.quynhlm.dev.be.model.entity.StoryReaction;
import com.quynhlm.dev.be.service.StoryReactionService;

import jakarta.validation.Valid;

@RestController
@RequestMapping(path = "api/story-reaction")
public class StoryReactionController {
    @Autowired
    private StoryReactionService storyReactionService;

    @PutMapping("")
    public ResponseEntity<ResponseObject<Boolean>> updateReaction(@RequestBody @Valid StoryReaction storyReaction) {
        ResponseObject<Boolean> result = new ResponseObject<>();
        storyReactionService.updateReaction(storyReaction);
        result.setData(true);
        result.setMessage("Update reaction successfully");
        return new ResponseEntity<ResponseObject<Boolean>>(result, HttpStatus.OK);
    }
}
