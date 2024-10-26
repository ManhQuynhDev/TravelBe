package com.quynhlm.dev.be.service;

import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.quynhlm.dev.be.core.exception.UnknowException;
import com.quynhlm.dev.be.core.exception.UserAccountExistingException;
import com.quynhlm.dev.be.core.exception.UserAccountNotFoundException;
import com.quynhlm.dev.be.model.dto.requestDTO.ChangeFullnameDTO;
import com.quynhlm.dev.be.model.dto.requestDTO.ChangePassDTO;
import com.quynhlm.dev.be.model.dto.requestDTO.IntrospectRequest;
import com.quynhlm.dev.be.model.dto.requestDTO.LoginDTO;
import com.quynhlm.dev.be.model.dto.responseDTO.OTPResponse;
import com.quynhlm.dev.be.model.dto.responseDTO.TokenResponse;
import com.quynhlm.dev.be.model.entity.User;
import com.quynhlm.dev.be.repositories.UserRepository;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JavaMailSender mailSender;

    private static final String SIGNER_KEY = "/q5Il7oI//Hiv4va97MQAtYOaktNo188-23WY12YVRCRGBEwYECRg0T6YcrEzYWb";

    private Map<String, OTPResponse> otpStorage = new HashMap<>();

    private static final long OTP_VALID_DURATION = 1; // 1 minute

    public TokenResponse login(LoginDTO request) throws UserAccountNotFoundException {
        User user = userRepository.findOneByUsername(request.getUsername());
        if (user == null) {
            throw new UserAccountNotFoundException(
                    "Username " + request.getUsername() + " not found. Please try another!");
        }
        PasswordEncoder passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();

        String token = generateToken(user);

        boolean isAuthenticated = passwordEncoder.matches(request.getPassword(), user.getPassword());
        TokenResponse response = new TokenResponse();
        if (isAuthenticated == false) { // Login failure
            response.setSuccess(isAuthenticated);
            response.setMessage("Login not successfully");
            return response;
        }
        response.setSuccess(isAuthenticated);
        response.setMessage("Authentication successful.");
        response.setToken(token);
        return response;
    }

    public User findAnUser(Long id) throws UserAccountNotFoundException {
        User user = userRepository.getAnUser(id);
        if (user == null) {
            throw new UserAccountExistingException("Id " + id + " not found . Please try another!");
        }
        return user;
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

    public void setNewPassWord(ChangePassDTO changePassDTO) throws UserAccountNotFoundException, UnknowException {
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

    public boolean checkUserToken(IntrospectRequest request) throws JOSEException, ParseException {
        String token = request.getToken();

        JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());

        SignedJWT signedJWT = SignedJWT.parse(token);

        Date expiration_time = signedJWT.getJWTClaimsSet().getExpirationTime();

        boolean isCheck = signedJWT.verify(verifier);

    return isCheck && expiration_time.after(new Date());
    }
    //ChangeFullname
    public void changeFullname(Long id, ChangeFullnameDTO changeName) throws UnknowException, UserAccountNotFoundException {
        if (userRepository.findById(id).isEmpty()) {
            throw new UserAccountNotFoundException("ID: " + id + " not found. Please try another!");
        } else {
            User user = userRepository.findOneById(id);
            if (user.getLastNameChangeDate() != null) {
                long daysSinceLastChange = ChronoUnit.DAYS.between(user.getLastNameChangeDate(),LocalDateTime.now());
                if (daysSinceLastChange < 30) {
                    throw new UnknowException("You can only change your name once every 30 days. Please try again after " + (30 - daysSinceLastChange) + " days.");
                }
            }

            user.setFullname(changeName.getFullname());
            user.setLastNameChangeDate(LocalDateTime.now());
            User saveName = userRepository.save(user);
            if (saveName.getId() == null) {
                throw new UnknowException("Transaction cannot complete!");
            }
        }
    }
}
