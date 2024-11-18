package com.quynhlm.dev.be.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quynhlm.dev.be.core.ResponseObject;
import com.quynhlm.dev.be.model.dto.requestDTO.PostRequestDTO;
import com.quynhlm.dev.be.model.dto.responseDTO.PostMediaDTO;
import com.quynhlm.dev.be.model.dto.responseDTO.PostResponseDTO;
import com.quynhlm.dev.be.model.dto.responseDTO.VideoPostDTO;
import com.quynhlm.dev.be.model.entity.Post;
import com.quynhlm.dev.be.service.PostService;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RequestMapping("/api/posts")
@RestController
public class PostController {
    @Autowired
    private PostService postService;

    // @PostMapping(value = "", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    // public ResponseEntity<ResponseObject<Void>> insertPost(@RequestPart("post")
    // String postJson,
    // @RequestPart(value = "files") List<MultipartFile> files,
    // @RequestPart("type") String type) {
    // postService.insertPost(post, files, type);
    // ResponseObject<Void> result = new ResponseObject<>();
    // result.setMessage("Create a new post successfully");
    // return new ResponseEntity<ResponseObject<Void>>(result, HttpStatus.OK);
    // }

    @PostMapping(value = "", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResponseObject<?>> insertPost(
            @RequestPart("post") String postJson,
            @RequestPart(value = "files") List<MultipartFile> files,
            @RequestPart("type") String type,
            HttpServletRequest request) {

        ObjectMapper objectMapper = new ObjectMapper();
        PostRequestDTO post = null;
        try {
            post = objectMapper.readValue(postJson, PostRequestDTO.class);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        Post postResponse = postService.insertPost(post, files, type);

        ResponseObject<Post> result = new ResponseObject<>();
        result.setMessage("Create a new post successfully");
        result.setStatus(true);
        result.setData(postResponse);
        return new ResponseEntity<ResponseObject<?>>(result, HttpStatus.OK);
    }

    @GetMapping("/friend_posts/{userId}")
    public Page<PostResponseDTO> getAllPostsAndSharedPosts(@PathVariable Integer userId, Pageable pageable) {
        return postService.getAllPostsAndSharedPosts(userId, pageable);
    }

    @GetMapping("/{post_id}")
    public ResponseEntity<ResponseObject<PostMediaDTO>> getMethodName(@PathVariable int post_id) {
        ResponseObject<PostMediaDTO> result = new ResponseObject<>();
        result.setData(postService.getAnPost(post_id));
        result.setMessage("Get an post by " + post_id + " successfully");
        result.setStatus(true);
        return new ResponseEntity<ResponseObject<PostMediaDTO>>(result, HttpStatus.OK);
    }

    @DeleteMapping("/{post_id}")
    public ResponseEntity<ResponseObject<?>> deleteStory(@PathVariable Integer post_id) {
        postService.deletePost(post_id);
        ResponseObject<Void> result = new ResponseObject<>();
        result.setMessage("Delete post successfully");
        result.setStatus(true);
        return new ResponseEntity<ResponseObject<?>>(result, HttpStatus.OK);
    }

    @PutMapping("/{post_id}")
    public ResponseEntity<ResponseObject<?>> updatePost(@PathVariable Integer post_id,
            @RequestPart("post") Post newPost,
            @RequestPart(value = "files", required = false) List<MultipartFile> files,
            @RequestPart(value = "type", required = false) String type) throws Exception {
        postService.updatePost(post_id, newPost, files, type);
        ResponseObject<Void> result = new ResponseObject<>();
        result.setMessage("Update post successfully");
        result.setStatus(true);
        return new ResponseEntity<ResponseObject<?>>(result, HttpStatus.OK);
    }

    @GetMapping("/videos")
    public Page<VideoPostDTO> getAllPostTypeVideo(Pageable pageable) {
        return postService.getAllPostTypeVideo(pageable);
    }
}