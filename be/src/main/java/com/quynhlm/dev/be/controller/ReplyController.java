package com.quynhlm.dev.be.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
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
import java.util.List;
import com.quynhlm.dev.be.core.exception.UnknownException;
import com.quynhlm.dev.be.model.dto.requestDTO.ReplyRequestDTO;
import com.quynhlm.dev.be.model.entity.Reply;
import com.quynhlm.dev.be.service.ReplyService;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;

@RequestMapping(path = "api/reply")
@RequiredArgsConstructor
@RestController
public class ReplyController {
    
    @Autowired
    private ReplyService replyService;

    @PostMapping("")
    public ResponseEntity<ResponseObject<Reply>> insertReply(
            @RequestPart("reply") String replyJson,
            @RequestPart(value = "imageFile", required = false) MultipartFile imageFile) throws UnknownException {

        ObjectMapper objectMapper = new ObjectMapper();
        ReplyRequestDTO reply = null;
        try {
            reply = objectMapper.readValue(replyJson, ReplyRequestDTO.class);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        Reply replyResponse = replyService.insertReply(reply, imageFile);
        ResponseObject<Reply> result = new ResponseObject<>();
        result.setMessage("Create a new reply successfully");
        result.setStatus(true);
        result.setData(replyResponse);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseObject<Void>> deleteReply(@PathVariable Integer id) {
        replyService.deleteReply(id);
        ResponseObject<Void> result = new ResponseObject<>();
        result.setMessage("Delete reply successfully");
        return new ResponseEntity<ResponseObject<Void>>(result, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResponseObject<Void>> updateReply(
            @PathVariable Integer id,
            @RequestParam String content,
            @RequestPart(value = "imageFile", required = false) MultipartFile imageFile) {
        replyService.updateReply(id, content, imageFile);
        ResponseObject<Void> result = new ResponseObject<>();
        result.setMessage("Update reply successfully");
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @GetMapping("/{comment_id}")
    public List<Reply> findReplyByCommentId(@PathVariable Integer comment_id) {
        return replyService.findReplyByCommentId(comment_id);
    }
}
