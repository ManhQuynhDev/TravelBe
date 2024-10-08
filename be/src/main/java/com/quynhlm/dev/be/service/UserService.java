package com.quynhlm.dev.be.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.quynhlm.dev.be.core.ResponseObject;
import com.quynhlm.dev.be.core.exception.UnknowException;
import com.quynhlm.dev.be.core.exception.UserAccountExistingException;
import com.quynhlm.dev.be.core.exception.UserAccountNotFoundException;
import com.quynhlm.dev.be.model.OTPResponse;
import com.quynhlm.dev.be.model.User;
import com.quynhlm.dev.be.model.dto.ChangePassDTO;
import com.quynhlm.dev.be.model.dto.LoginDTO;
import com.quynhlm.dev.be.repositories.UserRepository;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.nimbusds.jose.Payload;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import java.util.Map;
import java.util.Random;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JavaMailSender mailSender;

    private static final String SIGNER_KEY = "/q5Il7oI//Hiv4va97MQAtYOaktNo188-23WY12YVRCRGBEwYECRg0T6YcrEzYWb";

    private Map<String, OTPResponse> otpStorage = new HashMap<>();

    private static final long OTP_VALID_DURATION = 1; // 1 minute

    public ResponseObject<?> login(LoginDTO request) throws UserAccountExistingException {
        User user = userRepository.findOneByUsername(request.getUsername());
        if (user == null) {
            throw new UserAccountExistingException("User don't exist");
        }
        PasswordEncoder passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();

        String token = generateToken(user);

        boolean isAuthenticated = passwordEncoder.matches(request.getPassword(), user.getPassword());

        if (isAuthenticated == false) { // Login failure
            ResponseObject<Boolean> response = new ResponseObject<>();
            response.setData(isAuthenticated);
            response.setMessage("Login not successfully");
            return response;
        }
        ResponseObject<String> response = new ResponseObject<>();
        response.setData(token);
        return response;
    }

    public Page<User> getListData(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return userRepository.findAll(pageable);
    }

    public void register(User user) throws UserAccountExistingException, UnknowException {
        checkUserExists(user);

        PasswordEncoder passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        User savedUser = userRepository.save(user);
        if (savedUser.getId() == null) {
            throw new UnknowException("Transaction cannot complete!");
        }
    }

    private void checkUserExists(User user) throws UserAccountExistingException {
        if (!userRepository.findByEmail(user.getEmail()).isEmpty()) {
            throw new UserAccountExistingException("Email " + user.getEmail() + " already exist. Please try another!");
        }
        if (!userRepository.findByUsername(user.getUsername()).isEmpty()) {
            throw new UserAccountExistingException(
                    "Username " + user.getUsername() + " already exist. Please try another!");
        }
        if (!userRepository.findByPhoneNumber(user.getPhoneNumber()).isEmpty()) {
            throw new UserAccountExistingException(
                    "PhoneNumber " + user.getPhoneNumber() + " already exist. Please try another!");
        }
    }

    // Create token
    public String generateToken(User user) {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);
        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(user.getUsername())
                .issuer("quynhlm.dev@gmail.com")
                .issueTime(new Date())
                .expirationTime(new Date(System.currentTimeMillis() + 3600000))
                .claim("role", "USER")
                .build();

        Payload payload = new Payload(jwtClaimsSet.toJSONObject());
        JWSObject jwsObject = new JWSObject(header, payload);

        try {
            jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes()));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            throw new UnknowException("Token generation failed: " + e.getMessage());
        }
    }

    // Create OTP
    public void generateOTP(String email) throws UserAccountNotFoundException {
        List<User> foundUser = userRepository.findByEmail(email);
        if (foundUser.isEmpty()) {
            throw new UserAccountNotFoundException("Email " + email + " not found . Please try another!");
        }
        // Generate OTP
        String otp = String.valueOf(new Random().nextInt(900000) + 100000);

        // Create and store OTP details
        OTPResponse otpData = new OTPResponse(otp, LocalDateTime.now().plusMinutes(OTP_VALID_DURATION));
        otpStorage.put(email, otpData);

        // Send OTP via email
        sendEmail(email, "Your OTP Code", "Your OTP is: " + otp);
    }

    // Send email
    private void sendEmail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        mailSender.send(message);
    }

    public boolean canRequestNewOTP(String email) throws UserAccountNotFoundException {
        OTPResponse otpData = otpStorage.get(email);
        if (otpData != null) {
            return otpData.getExpiryTime().minusMinutes(OTP_VALID_DURATION).isBefore(LocalDateTime.now());
        }
        return true;
    }

    public boolean validateOTP(String email, String otp) {
        OTPResponse otpData = otpStorage.get(email);
        if (otpData != null) {
            if (otpData.getExpiryTime().isAfter(LocalDateTime.now())) {
                return otpData.getOtp().equals(otp);
            }
        }
        return false;
    }

    public void setNewPassWord(ChangePassDTO changePassDTO) throws UserAccountNotFoundException , UnknowException{
        User foundUser = userRepository.findOneByEmail(changePassDTO.getEmail());

        if (foundUser == null) {
            throw new UserAccountNotFoundException(
                    "User with " + changePassDTO.getEmail() + " not found . Please try another!");
        }

        PasswordEncoder passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
        String newEncodedPassword = passwordEncoder.encode(changePassDTO.getPassword());

        foundUser.setPassword(newEncodedPassword);
        User savedUser = userRepository.save(foundUser);
        if (savedUser.getId() == null) {
            throw new UnknowException("Transaction cannot complete!");
        }
    }
}
