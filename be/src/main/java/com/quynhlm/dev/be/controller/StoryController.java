package com.quynhlm.dev.be.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.quynhlm.dev.be.core.ResponseObject;
import com.quynhlm.dev.be.model.entity.Story;
import com.quynhlm.dev.be.service.StoryService;

@RestController
@RequestMapping("/stories")
public class StoryController {

    @Autowired
    private StoryService storyService;

    @PostMapping("")
    public ResponseEntity<ResponseObject<Void>> insertStory(
            @RequestPart("story") Story story,
            @RequestPart(value = "mediaUrl", required = false) MultipartFile mediaUrl,
            @RequestPart(value = "musicFile", required = false) MultipartFile musicFile) throws Exception {

        storyService.insertStory(story, mediaUrl, musicFile);
        ResponseObject<Void> result = new ResponseObject<>();
        result.setMessage("Create a new story successfully");

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseObject<Void>> deleteStory(@PathVariable int id) {
        storyService.deleteStory(id);
        ResponseObject<Void> result = new ResponseObject<>();
        result.setMessage("Delete story successfully");
        return new ResponseEntity<ResponseObject<Void>>(result, HttpStatus.OK);
    }
}
