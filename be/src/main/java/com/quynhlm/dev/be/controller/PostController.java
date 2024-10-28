package com.quynhlm.dev.be.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.quynhlm.dev.be.core.ResponseObject;
import com.quynhlm.dev.be.model.dto.responseDTO.PostMediaDTO;
import com.quynhlm.dev.be.model.entity.Post;
import com.quynhlm.dev.be.service.PostService;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RequestMapping("posts")
@RestController
public class PostController {
    @Autowired
    private PostService postService;

    @PostMapping("")
    public ResponseEntity<ResponseObject<Void>> insertPost(@RequestPart("post") Post post,
            @RequestPart("files") List<MultipartFile> files,
            @RequestPart("type") String type) throws Exception {
        postService.insertPost(post, files, type);
        ResponseObject<Void> result = new ResponseObject<>();
        result.setMessage("Create a new post successfully");
        return new ResponseEntity<ResponseObject<Void>>(result, HttpStatus.OK);
    }

    @GetMapping("")
    public Page<PostMediaDTO> getPostsWithMedia(
            @PageableDefault(page = 0, size = 2) Pageable pageable) {
        return postService.getPostsWithMedia(pageable);
    }

    @DeleteMapping("/{post_id}")
    public ResponseEntity<ResponseObject<Void>> deleteStory(@PathVariable int post_id) {
        postService.deletePost(post_id);
        ResponseObject<Void> result = new ResponseObject<>();
        result.setMessage("Delete post successfully");
        return new ResponseEntity<ResponseObject<Void>>(result, HttpStatus.OK);
    }
}