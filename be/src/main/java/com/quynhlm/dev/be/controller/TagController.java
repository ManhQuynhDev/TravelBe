package com.quynhlm.dev.be.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.quynhlm.dev.be.core.ResponseObject;
import com.quynhlm.dev.be.model.entity.Tag;
import com.quynhlm.dev.be.service.TagService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;

@RestController
@RequestMapping(path = "api/tags")
public class TagController {
    @Autowired
    private TagService tagService;

    

    @PostMapping("")
    public ResponseEntity<ResponseObject<Void>> tagWithFriend(@RequestBody Tag tag) {
        ResponseObject<Void> result = new ResponseObject<>();
        tagService.tagWithFriend(tag);
        result.setMessage("Create a new tag successfully");
        return new ResponseEntity<ResponseObject<Void>>(result, HttpStatus.OK);
    }

    @DeleteMapping("")
    public ResponseEntity<ResponseObject<Void>> deleteUserFormTag(@RequestParam Integer id,
            @RequestParam Integer userId) {
        ResponseObject<Void> result = new ResponseObject<>();
        tagService.deleteUserFormTag(id, userId);
        result.setMessage("Delete user form tag successfully");
        return new ResponseEntity<ResponseObject<Void>>(result, HttpStatus.OK);
    }
}
