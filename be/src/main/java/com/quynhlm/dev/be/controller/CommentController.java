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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quynhlm.dev.be.core.ResponseObject;
import com.quynhlm.dev.be.model.dto.requestDTO.CommentRequestDTO;
import com.quynhlm.dev.be.model.dto.responseDTO.CommentResponseDTO;
import com.quynhlm.dev.be.model.entity.Comment;
import com.quynhlm.dev.be.service.CommentService;

import org.springframework.web.bind.annotation.PutMapping;

import com.quynhlm.dev.be.core.exception.UnknownException;

@RestController
@RequestMapping(path = "/api/comments")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @GetMapping("")
    public Page<Comment> getComments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "2") int size) {
        return commentService.getListData(page, size);
    }

    @PostMapping(value = "", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResponseObject<Comment>> insertComment(
            @RequestPart("comment") String commentJson,
            @RequestPart(value = "imageFile", required = false) MultipartFile imageFile) throws UnknownException {

        ObjectMapper objectMapper = new ObjectMapper();
        CommentRequestDTO comment = null;
        try {
            comment = objectMapper.readValue(commentJson, CommentRequestDTO.class);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        Comment commentResponse = commentService.insertComment(comment, imageFile);
        ResponseObject<Comment> result = new ResponseObject<>();
        result.setMessage("Create a new comment successfully");
        result.setData(commentResponse);
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
            @RequestParam String content,
            @RequestPart(value = "imageFile", required = false) MultipartFile imageFile) {
        commentService.updateComment(id, content, imageFile);
        ResponseObject<Void> result = new ResponseObject<>();
        result.setMessage("Update comment successfully");
        result.setStatus(true);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseObject<Comment>> findAnComment(@PathVariable Integer id) {
        ResponseObject<Comment> result = new ResponseObject<>();
        result.setMessage("Get an comment with id " + id + " successfully");
        result.setData(commentService.findAnComment(id));
        result.setStatus(true);
        return new ResponseEntity<ResponseObject<Comment>>(result, HttpStatus.OK);
    }

    @GetMapping("/postId")
    public Page<CommentResponseDTO> foundCommentWithPostId(@RequestParam("postId") Integer postId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "2") int size) {
        return commentService.fetchCommentWithPostId(postId, page, size);
    }

    @GetMapping("/shareId")
    public Page<CommentResponseDTO> foundCommentWithShareId(@RequestParam("shareId") Integer shareId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "2") int size) {
        return commentService.fetchCommentWithShareId(shareId, page, size);
    }
}
