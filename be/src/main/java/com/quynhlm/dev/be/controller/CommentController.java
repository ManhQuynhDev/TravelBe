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

import com.quynhlm.dev.be.core.ResponseObject;
import com.quynhlm.dev.be.model.entity.Comment;
import com.quynhlm.dev.be.model.entity.User;
import com.quynhlm.dev.be.service.CommentService;

import org.springframework.web.bind.annotation.PutMapping;

import com.quynhlm.dev.be.core.exception.UnknownException;

@RestController
@RequestMapping("/comments")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @GetMapping("")
    public Page<Comment> getComments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "2") int size) {
        return commentService.getListData(page, size);
    }

    @PostMapping("")
    public ResponseEntity<ResponseObject<Void>> insertComment(
            @RequestPart("comment") Comment comment,
            @RequestPart(value = "imageFile", required = false) MultipartFile imageFile) throws UnknownException {
        commentService.insertComment(comment, imageFile);
        ResponseObject<Void> result = new ResponseObject<>();
        result.setMessage("Create a new comment successfully");
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseObject<Void>> deleteComment(@PathVariable Integer id) {
        commentService.deleteComment(id);
        ResponseObject<Void> result = new ResponseObject<>();
        result.setMessage("Delete comment successfully");
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
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    
    @GetMapping("/{id}")
    public ResponseEntity<ResponseObject<Comment>> findAnComment(@PathVariable Integer id) {
        ResponseObject<Comment> result = new ResponseObject<>();
        result.setMessage("Get an comment with id " + id + " successfully");
        result.setData(commentService.findAnComment(id));
        return new ResponseEntity<ResponseObject<Comment>>(result, HttpStatus.OK);
    }
    
}
