package com.quynhlm.dev.be.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.quynhlm.dev.be.core.ResponseObject;
import com.quynhlm.dev.be.model.User;
import com.quynhlm.dev.be.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/onboarding")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping(path = "/register")
    public ResponseEntity<ResponseObject<Void>> register(@RequestBody @Valid User user) {
        userService.register(user);
        ResponseObject<Void> result = new ResponseObject<>();
        result.setMessage("Create a new account successfully");
        return new ResponseEntity<ResponseObject<Void>>(result, HttpStatus.OK);
    }
}
