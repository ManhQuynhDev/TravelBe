package com.quynhlm.dev.be.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quynhlm.dev.be.core.ResponseObject;
import com.quynhlm.dev.be.model.dto.requestDTO.StoryRequestDTO;
import com.quynhlm.dev.be.model.dto.responseDTO.FriendStoryResponseDTO;
import com.quynhlm.dev.be.model.dto.responseDTO.StoryResponseDTO;
import com.quynhlm.dev.be.model.entity.Story;
import com.quynhlm.dev.be.service.StoryService;

@RestController
@RequestMapping("/api/stories")
public class StoryController {

    @Autowired
    private StoryService storyService;

    @GetMapping("")
    public Page<StoryResponseDTO> getStories(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "2") int size) {
        return storyService.getAllStory(page, size);
    }

    @GetMapping("/user-create/{user_id}")
    public Page<StoryResponseDTO> getAllStoryCreateByUserId(
            @PathVariable Integer user_id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "2") int size) {
        return storyService.getAllStoryCreateByUserId(user_id, page, size);
    }

    @GetMapping("/friend_story/{userId}")
    public Page<FriendStoryResponseDTO> getStoriesByUserId(
            @PathVariable Integer userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "2") int size) {
        return storyService.fetchFriendStoriesByUserId(userId, page, size);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseObject<Story>> getAnStory(@PathVariable int id) {
        ResponseObject<Story> result = new ResponseObject<>();
        result.setMessage("Get an story successfully");
        result.setData(storyService.getAnStory(id));
        result.setStatus(true);
        return new ResponseEntity<ResponseObject<Story>>(result, HttpStatus.OK);
    }

    @PostMapping("")
    public ResponseEntity<ResponseObject<Story>> insertStory(
            @RequestPart("story") String storyJson,
            @RequestPart(value = "mediaUrl", required = true) MultipartFile mediaUrl,
            @RequestPart(value = "musicFile", required = true) MultipartFile musicFile) throws Exception {

        ObjectMapper objectMapper = new ObjectMapper();
        StoryRequestDTO story = null;
        try {
            story = objectMapper.readValue(storyJson, StoryRequestDTO.class);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        Story storyResponse = storyService.insertStory(story, mediaUrl, musicFile);
        ResponseObject<Story> result = new ResponseObject<>();
        result.setMessage("Create a new story successfully");
        result.setStatus(true);
        result.setData(storyResponse);
        return new ResponseEntity<ResponseObject<Story>>(result, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseObject<Void>> deleteStory(@PathVariable int id) {
        storyService.deleteStory(id);
        ResponseObject<Void> result = new ResponseObject<>();
        result.setMessage("Delete story successfully");
        return new ResponseEntity<ResponseObject<Void>>(result, HttpStatus.OK);
    }
}
