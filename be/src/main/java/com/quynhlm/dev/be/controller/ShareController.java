package com.quynhlm.dev.be.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.quynhlm.dev.be.core.ResponseObject;
import com.quynhlm.dev.be.model.dto.responseDTO.PostResponseDTO;
import com.quynhlm.dev.be.model.entity.Share;
import com.quynhlm.dev.be.service.ShareService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping(path = "/api/share")
public class ShareController {
    @Autowired
    private ShareService shareService;

    @PostMapping("")
    public ResponseEntity<ResponseObject<PostResponseDTO>> sharePost(@RequestBody Share share) {
        ResponseObject<PostResponseDTO> result = new ResponseObject<>();
        PostResponseDTO response = shareService.sharePost(share);
        result.setMessage("Create a share successfully");
        result.setData(response);
        result.setStatus(true);
        return new ResponseEntity<ResponseObject<PostResponseDTO>>(result, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseObject<Void>> deleteShare(@PathVariable Integer id) {
        shareService.deleteShare(id);
        ResponseObject<Void> result = new ResponseObject<>();
        result.setMessage("Delete share successfully");
        result.setStatus(true);
        return new ResponseEntity<ResponseObject<Void>>(result, HttpStatus.OK);
    }

    @GetMapping("/{share_id}/{user_id}")
    public ResponseEntity<ResponseObject<PostResponseDTO>> getAnShare(@PathVariable Integer share_id,
            @PathVariable Integer user_id) {
        ResponseObject<PostResponseDTO> result = new ResponseObject<>();
        result.setData(shareService.getAnShare(share_id, user_id));
        result.setMessage("Get share post by " + share_id + " successfully");
        result.setStatus(true);
        return new ResponseEntity<ResponseObject<PostResponseDTO>>(result, HttpStatus.OK);
    }
}
