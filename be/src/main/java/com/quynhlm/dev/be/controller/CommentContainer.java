package com.quynhlm.dev.be.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quynhlm.dev.be.core.ResponseObject;
import com.quynhlm.dev.be.core.exception.UnknownException;
import com.quynhlm.dev.be.model.dto.requestDTO.FeedBackRequestDTO;
import com.quynhlm.dev.be.model.dto.responseDTO.CommentResponseDTO;
import com.quynhlm.dev.be.service.CommentService;

@RestController
@RequestMapping(path = "/api/comments")
public class CommentContainer {
    @Autowired
    private CommentService commentService;

    @PostMapping(value = "", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResponseObject<CommentResponseDTO>> insertComment(
            @RequestPart("comment") String commentJson,
            @RequestPart(value = "file", required = false) MultipartFile imageFile) throws UnknownException {

        ObjectMapper objectMapper = new ObjectMapper();
        FeedBackRequestDTO comment = null;
        try {
            comment = objectMapper.readValue(commentJson, FeedBackRequestDTO.class);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        CommentResponseDTO response = commentService.insertComment(comment, imageFile);
        ResponseObject<CommentResponseDTO> result = new ResponseObject<>();
        result.setMessage("Create a new comment successfully");
        result.setData(response);
        result.setStatus(true);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseObject<Void>> deleteComment(@PathVariable Integer id) {
        commentService.deleteComment(id);
        ResponseObject<Void> result = new ResponseObject<>();
        result.setMessage("Delete comment successfully");
        result.setStatus(true);
        return new ResponseEntity<ResponseObject<Void>>(result, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResponseObject<Void>> updateComment(
            @PathVariable Integer id,
            @RequestPart(required = false) String content,
            @RequestPart(value = "file", required = false) MultipartFile imageFile) {
        commentService.updateComment(id, content, imageFile);
        ResponseObject<Void> result = new ResponseObject<>();
        result.setMessage("Update comment successfully");
        result.setStatus(true);
        return new ResponseEntity<ResponseObject<Void>>(result, HttpStatus.OK);
    }

    @GetMapping("/{id}/{userId}")
    public ResponseEntity<ResponseObject<CommentResponseDTO>> findAnComment(@PathVariable Integer id,
            @PathVariable Integer userId) {
        ResponseObject<CommentResponseDTO> result = new ResponseObject<>();
        result.setMessage("Get an comment with id " + id + " successfully");
        result.setData(commentService.findAnComment(id, userId));
        result.setStatus(true);
        return new ResponseEntity<ResponseObject<CommentResponseDTO>>(result, HttpStatus.OK);
    }

    @GetMapping("/postId")
    public Page<CommentResponseDTO> foundCommentWithPostId(@RequestParam Integer postId, @RequestParam Integer userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "2") int size) {
        return commentService.fetchCommentWithPostId(postId, userId, page, size);
    }
}
