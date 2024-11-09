package com.quynhlm.dev.be.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.nimbusds.jose.JOSEException;
import com.quynhlm.dev.be.core.ResponseObject;
import com.quynhlm.dev.be.core.validation.ValidStatusAccountType;
import com.quynhlm.dev.be.model.dto.requestDTO.ChangePassDTO;
import com.quynhlm.dev.be.model.dto.requestDTO.ConfirmEmailDTO;
import com.quynhlm.dev.be.model.dto.requestDTO.IntrospectRequest;
import com.quynhlm.dev.be.model.dto.requestDTO.LoginDTO;
import com.quynhlm.dev.be.model.dto.requestDTO.UpdateProfileDTO;
import com.quynhlm.dev.be.model.dto.requestDTO.VerifyDTO;
import com.quynhlm.dev.be.model.dto.responseDTO.TokenResponse;
import com.quynhlm.dev.be.model.entity.User;
import com.quynhlm.dev.be.service.UserService;

import java.text.ParseException;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/onboarding")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping(path = "/register")
    public ResponseEntity<ResponseObject<Void>> register(@RequestBody User user) {
        userService.register(user);
        ResponseObject<Void> result = new ResponseObject<>();
        result.setMessage("Create a new account successfully");
        return new ResponseEntity<ResponseObject<Void>>(result, HttpStatus.OK);
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody LoginDTO request) {
        TokenResponse response = userService.login(request);
        return new ResponseEntity<TokenResponse>(response, HttpStatus.OK);
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<ResponseObject<User>> findAnUser(@PathVariable Integer id) {
        ResponseObject<User> result = new ResponseObject<>();
        result.setMessage("Get an user with id " + id + " successfully");
        result.setData(userService.findAnUser(id));
        return new ResponseEntity<ResponseObject<User>>(result, HttpStatus.OK);
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

    // Email
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

    // Token
    @PostMapping("/auth/token")
    public ResponseEntity<ResponseObject<Boolean>> introspect(@RequestBody IntrospectRequest request) {
        ResponseObject<Boolean> response = new ResponseObject<>();
        boolean isCheckUserToken = false;
        try {
            isCheckUserToken = userService.checkUserToken(request);
            response.setData(isCheckUserToken);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (JOSEException | ParseException e) {
            response.setData(isCheckUserToken);
            e.printStackTrace();
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/change-full-name/{id}")
    public ResponseEntity<ResponseObject<Void>> changeFullname(@PathVariable Integer id,
            @RequestParam String newName) {
        userService.changeFullname(id, newName);
        ResponseObject<Void> response = new ResponseObject<>();
        response.setMessage("Change Fullname successfully.");
        return new ResponseEntity<ResponseObject<Void>>(response, HttpStatus.OK);
    }

    @PostMapping("/change-profile/{id}")
    public ResponseEntity<ResponseObject<Void>> changeProfile(@PathVariable int id,
            @RequestPart("updateDTO") UpdateProfileDTO updateDTO,
            @RequestPart(value = "avatar", required = false) MultipartFile imageFile) {
        userService.changeProfile(id, updateDTO, imageFile);
        ResponseObject<Void> response = new ResponseObject<>();
        response.setMessage("User profile updated successfully.");
        return new ResponseEntity<ResponseObject<Void>>(response, HttpStatus.OK);
    }

    @PutMapping("/status-account/{id}/status")
    public ResponseEntity<ResponseObject<Void>> switchStatusUser(@PathVariable Integer id,
            @RequestParam @Valid @ValidStatusAccountType String status) {
        userService.switchStatusUser(id, status);
        ResponseObject<Void> response = new ResponseObject<>();
        response.setMessage("Switch status user successfully.");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping(path = "/createManager")
    public ResponseEntity<ResponseObject<Void>> createManager(@RequestBody User user) {
        userService.createManager(user);
        ResponseObject<Void> result = new ResponseObject<>();
        result.setMessage("Create a new account successfully");
        return new ResponseEntity<ResponseObject<Void>>(result, HttpStatus.OK);
    }
}
