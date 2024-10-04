package com.quynhlm.dev.be.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.quynhlm.dev.be.core.ResponseObject;
import com.quynhlm.dev.be.model.User;
import com.quynhlm.dev.be.model.dto.ChangePassDTO;
import com.quynhlm.dev.be.model.dto.ConfirmEmailDTO;
import com.quynhlm.dev.be.model.dto.LoginDTO;
import com.quynhlm.dev.be.model.dto.VerifyDTO;
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

    @PostMapping("/login")
    public ResponseEntity<ResponseObject<?>> login(@RequestBody LoginDTO request) {
        ResponseObject<?> response = userService.login(request);
        return new ResponseEntity<ResponseObject<?>>(response, HttpStatus.OK);
    }

    @GetMapping("/users")
    public Page<User> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "2") int size) {
        return userService.getListData(page, size);
    }

    @PostMapping("/send")
    public ResponseEntity<String> sendOTP(@RequestBody ConfirmEmailDTO request) {
        if (userService.canRequestNewOTP(request.getEmail())) {
            userService.generateOTP(request.getEmail());
            return ResponseEntity.ok("OTP sent to email: " + request.getEmail());
        } else {
            return ResponseEntity.badRequest().body("Please wait 1 minute before requesting a new OTP.");
        }
    }

    @PostMapping("/verify")
    public ResponseEntity<Boolean> verifyOTP(@RequestBody VerifyDTO verify) {
        if (userService.validateOTP(verify.getEmail(), verify.getOtp())) {
            return ResponseEntity.ok(true);
        } else {
            return ResponseEntity.badRequest().body(false);
        }
    }

    @PostMapping("/set-password")
    public ResponseEntity<ResponseObject<Void>> changePassword(@RequestBody ChangePassDTO changePassDTO) {
        ResponseObject<Void> result = new ResponseObject<>();
        userService.setNewPassWord(changePassDTO);
        result.setMessage("Change password successfully");
        return new ResponseEntity<ResponseObject<Void>>(result, HttpStatus.OK);
    }
}
