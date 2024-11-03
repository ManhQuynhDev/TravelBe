package com.quynhlm.dev.be.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.quynhlm.dev.be.core.ResponseObject;
import com.quynhlm.dev.be.model.entity.Share;
import com.quynhlm.dev.be.service.ShareService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping(path = "/api/share")
public class ShareController {
    @Autowired
    private ShareService shareService;

    @PostMapping("")
    public ResponseEntity<ResponseObject<Void>> sharePost(@RequestBody Share share) {
        ResponseObject<Void> result = new ResponseObject<>();
        shareService.sharePost(share);
        result.setMessage("Create a share successfully");
        return new ResponseEntity<ResponseObject<Void>>(result, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseObject<Void>> deleteShare(@PathVariable Integer id) {
        shareService.deleteShare(id);
        ResponseObject<Void> result = new ResponseObject<>();
        result.setMessage("Delete share successfully");
        return new ResponseEntity<ResponseObject<Void>>(result, HttpStatus.OK);
    }
}
