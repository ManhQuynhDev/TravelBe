package com.quynhlm.dev.be.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JOSEException;
import com.quynhlm.dev.be.core.ResponseObject;
import com.quynhlm.dev.be.model.dto.requestDTO.ChangePassDTO;
import com.quynhlm.dev.be.model.dto.requestDTO.ConfirmEmailDTO;
import com.quynhlm.dev.be.model.dto.requestDTO.LoginDTO;
import com.quynhlm.dev.be.model.dto.requestDTO.RegisterDTO;
import com.quynhlm.dev.be.model.dto.requestDTO.UpdateProfileDTO;
import com.quynhlm.dev.be.model.dto.requestDTO.VerifyDTO;
import com.quynhlm.dev.be.model.dto.responseDTO.TokenResponse;
import com.quynhlm.dev.be.model.dto.responseDTO.UserInvitationResponseDTO;
import com.quynhlm.dev.be.model.dto.responseDTO.UserResponseDTO;
import com.quynhlm.dev.be.model.dto.responseDTO.UserStatisticalRegister;
import com.quynhlm.dev.be.model.entity.User;
import com.quynhlm.dev.be.service.UserService;

import java.text.ParseException;
import java.util.List;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@CrossOrigin
@RequestMapping("/onboarding")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/users/role-admin")
    public List<User> getAllListUser() {
        return userService.getAllListUser();
    }

    @PostMapping(path = "/register")
    public ResponseEntity<ResponseObject<Void>> register(@RequestBody @Valid RegisterDTO user) {
        userService.register(user);
        ResponseObject<Void> result = new ResponseObject<>();
        result.setStatus(true);
        result.setMessage("Create a new account successfully");
        return new ResponseEntity<ResponseObject<Void>>(result, HttpStatus.OK);
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse<UserResponseDTO>> login(@RequestBody LoginDTO request) {
        TokenResponse<UserResponseDTO> response = userService.login(request);
        return new ResponseEntity<TokenResponse<UserResponseDTO>>(response, HttpStatus.OK);
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<ResponseObject<UserResponseDTO>> findAnUser(@PathVariable Integer id) {
        ResponseObject<UserResponseDTO> result = new ResponseObject<>();
        result.setMessage("Get an user with id " + id + " successfully");
        result.setData(userService.findAnUser(id));
        result.setStatus(true);
        return new ResponseEntity<ResponseObject<UserResponseDTO>>(result, HttpStatus.OK);
    }

    @GetMapping("/users")
    public Page<UserResponseDTO> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "2") int size) {
        return userService.getListData(page, size);
    }

    @GetMapping("/get_all_invitation/{user_id}")
    public Page<UserInvitationResponseDTO> getAllInvitation(
            @PathVariable Integer user_id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "2") int size) {
        return userService.getAllInvitation(user_id, page, size);
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
    public ResponseEntity<ResponseObject<Boolean>> changePassword(@RequestBody ChangePassDTO changePassDTO) {
        ResponseObject<Boolean> result = new ResponseObject<>();
        userService.setNewPassWord(changePassDTO);
        result.setData(true);
        result.setMessage("Change password successfully");
        result.setStatus(true);
        return new ResponseEntity<ResponseObject<Boolean>>(result, HttpStatus.OK);
    }

    @DeleteMapping("/delete-user/{userId}")
    public ResponseEntity<ResponseObject<Void>> deleteUser(@PathVariable Integer userId) {
        ResponseObject<Void> result = new ResponseObject<>();
        userService.deleteUser(userId);
        result.setMessage("Delete user successfully");
        result.setStatus(true);
        return new ResponseEntity<ResponseObject<Void>>(result, HttpStatus.OK);
    }

    // Token
    @PostMapping("/auth/token")
    public ResponseEntity<ResponseObject<Boolean>> introspect(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestParam(required = false) String deviceToken, @RequestParam(required = false) String currentDevice) {
        ResponseObject<Boolean> response = new ResponseObject<>();
        boolean isCheckUserToken = false;
        try {
            String token = authorizationHeader.startsWith("Bearer ")
                    ? authorizationHeader.substring(7)
                    : authorizationHeader;
            isCheckUserToken = userService.checkUserToken(token, deviceToken, currentDevice);
            response.setData(isCheckUserToken);
            response.setMessage("Verify token successfully");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (JOSEException | ParseException e) {
            response.setMessage("Verify not token successfully");
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
        response.setStatus(true);
        response.setMessage("Change Fullname successfully.");
        return new ResponseEntity<ResponseObject<Void>>(response, HttpStatus.OK);
    }

    @PostMapping("/change-profile/{id}")
    public ResponseEntity<ResponseObject<Void>> changeProfile(@PathVariable int id,
            @RequestPart("updateDTO") @Valid String updateDTOJson,
            @RequestPart(value = "avatar", required = false) MultipartFile imageFile) {

        ObjectMapper objectMapper = new ObjectMapper();
        UpdateProfileDTO updateProfileDTO = null;
        try {
            updateProfileDTO = objectMapper.readValue(updateDTOJson, UpdateProfileDTO.class);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        userService.changeProfile(id, updateProfileDTO, imageFile);
        ResponseObject<Void> response = new ResponseObject<>();
        response.setMessage("User profile updated successfully.");
        response.setStatus(true);
        return new ResponseEntity<ResponseObject<Void>>(response, HttpStatus.OK);
    }

    @PutMapping("/locked-account/{id}/isLocked")
    public ResponseEntity<ResponseObject<Void>> switchLockedUser(@PathVariable Integer id,
            @RequestParam String isLocked) {
        userService.switchIsLockedUser(id, isLocked);
        ResponseObject<Void> response = new ResponseObject<>();
        response.setStatus(true);
        response.setMessage("Switch status user successfully.");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping("/status-account/{id}/status")
    public ResponseEntity<ResponseObject<Void>> switchStatusUser(@PathVariable Integer id,
            @RequestParam String status) {
        userService.switchStatusUser(id, status);
        ResponseObject<Void> response = new ResponseObject<>();
        response.setStatus(true);
        response.setMessage("Switch status user successfully.");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping(path = "/createManager")
    public ResponseEntity<ResponseObject<Void>> createManager(@RequestBody User user) {
        userService.createManager(user);
        ResponseObject<Void> result = new ResponseObject<>();
        result.setStatus(true);
        result.setMessage("Create a new account successfully");
        return new ResponseEntity<ResponseObject<Void>>(result, HttpStatus.OK);
    }

    @GetMapping("/users/manager")
    public Page<User> getAllManager(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "2") int size) {
        return userService.getAllListManager(page, size);
    }

    @GetMapping("/statistical_register/{year}")
    public List<UserStatisticalRegister> getUserRegistrationCountByMonth(@PathVariable int year) {
        return userService.getUserRegistrationCountByMonth(year);
    }

    @GetMapping("/users/fullname/{userId}")
    public String getUserFullName(@PathVariable Integer userId) {
        return userService.getUserFullname(userId);
    }

    @PutMapping("/register-device")
    public ResponseEntity<ResponseObject<Void>> registerDevice(@RequestParam Integer userId,
            @RequestParam String deviceToken) {
        userService.registerDevice(userId, deviceToken);
        ResponseObject<Void> response = new ResponseObject<>();
        response.setStatus(true);
        response.setMessage("Device token registered successfully.");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/{id}/lock-status")
    public ResponseEntity<ResponseObject<Void>> updateLockStatus(@PathVariable Integer id,
            @RequestParam String isLock) {
        userService.switchIsLockedUser(id, isLock);
        ResponseObject<Void> response = new ResponseObject<>();
        response.setStatus(true);
        response.setMessage("Transaction successfully.");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
