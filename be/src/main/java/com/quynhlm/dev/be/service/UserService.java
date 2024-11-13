package com.quynhlm.dev.be.service;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.StringJoiner;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
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
import com.quynhlm.dev.be.core.exception.MethodNotValidException;
import com.quynhlm.dev.be.core.exception.UnknownException;
import com.quynhlm.dev.be.core.exception.UserAccountExistingException;
import com.quynhlm.dev.be.core.exception.UserAccountNotFoundException;
import com.quynhlm.dev.be.enums.AccountStatus;
import com.quynhlm.dev.be.enums.Role;
import com.quynhlm.dev.be.model.dto.requestDTO.ChangePassDTO;
import com.quynhlm.dev.be.model.dto.requestDTO.IntrospectRequest;
import com.quynhlm.dev.be.model.dto.requestDTO.LoginDTO;
import com.quynhlm.dev.be.model.dto.requestDTO.UpdateProfileDTO;
import com.quynhlm.dev.be.model.dto.responseDTO.OTPResponse;
import com.quynhlm.dev.be.model.dto.responseDTO.TokenResponse;
import com.quynhlm.dev.be.model.dto.responseDTO.UserResponseDTO;
import com.quynhlm.dev.be.model.entity.User;
import com.quynhlm.dev.be.repositories.UserRepository;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private AmazonS3 amazonS3;

    @Value("${aws.s3.bucketName}")
    private String bucketName;

    private static final String SIGNER_KEY = "/q5Il7oI//Hiv4va97MQAtYOaktNo188-23WY12YVRCRGBEwYECRg0T6YcrEzYWb";

    private Map<String, OTPResponse> otpStorage = new HashMap<>();

    private static final long OTP_VALID_DURATION = 1; // 1 minute

    // Login Check
    public TokenResponse<UserResponseDTO> login(LoginDTO request) throws UserAccountNotFoundException {
        User user = userRepository.findOneByEmail(request.getEmail());
        if (user == null) {
            throw new UserAccountNotFoundException(
                    "Email " + request.getEmail() + " not found. Please try another!");
        }
        PasswordEncoder passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();

        String token = generateToken(user);

        boolean isAuthenticated = passwordEncoder.matches(request.getPassword(), user.getPassword());
        TokenResponse<UserResponseDTO> response = new TokenResponse<>();
        if (isAuthenticated == false) { // Login failure
            response.setStatus(isAuthenticated);
            response.setMessage("Login not successfully");
            return response;
        }
        response.setStatus(isAuthenticated);
        response.setMessage("Authentication successful.");
        response.setToken(token);

        UserResponseDTO userResponseDTO = new UserResponseDTO();
        userResponseDTO.setId(user.getId());
        userResponseDTO.setFullname(user.getFullname());
        userResponseDTO.setEmail(user.getEmail());
        userResponseDTO.setPhoneNumber(user.getPhoneNumber());
        userResponseDTO.setRoles(user.getRoles());
        userResponseDTO.setIsLocked(user.getIsLocked());
        userResponseDTO.setCreate_at(user.getCreate_at());
        userResponseDTO.setAvatarUrl(user.getAvatarUrl());
        response.setUserInfo(userResponseDTO);
        return response;
    }

    @PostAuthorize("hasRole('ADMIN') or returnObject.username == authentication.name")
    public User findAnUser(Integer id) throws UserAccountNotFoundException {
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

    public void register(User user) throws UserAccountExistingException, UnknownException {
        if (!userRepository.findByEmail(user.getEmail()).isEmpty()) {
            throw new UserAccountExistingException("Email " + user.getEmail() + " already exist. Please try another!");
        }

        user.setIsLocked(AccountStatus.OPEN.name());

        PasswordEncoder passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        HashSet<String> roles = new HashSet<>();
        roles.add(Role.USER.name());
        user.setRoles(roles);

        User savedUser = userRepository.save(user);
        if (savedUser.getId() == null) {
            throw new UnknownException("Transaction cannot complete!");
        }
    }

    private boolean isValidFileType(String contentType) {
        return contentType.startsWith("image/") || contentType.startsWith("video/");
    }

    // Create token
    public String generateToken(User user) {
        // Create JWT header with the HS512 algorithm
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);

        // Build JWT claims
        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(user.getFullname())
                .issuer("quynhlm.dev@gmail.com")
                .issueTime(new Date())
                .expirationTime(new Date(System.currentTimeMillis() + 3600000)) // 1 hour expiration
                .claim("scope", buildScope(user))
                .build();

        // Prepare payload and JWS object
        Payload payload = new Payload(jwtClaimsSet.toJSONObject());
        JWSObject jwsObject = new JWSObject(header, payload);

        try {
            // Sign the JWS object with the secret key
            jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes()));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            throw new UnknownException("Token generation failed: " + e.getMessage());
        }
    }

    private String buildScope(User user) {
        StringJoiner stringJoiner = new StringJoiner(" ");

        // Check if roles are not empty
        if (!CollectionUtils.isEmpty(user.getRoles())) {
            user.getRoles().forEach(stringJoiner::add);
        }

        return stringJoiner.toString();
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

    public void setNewPassWord(ChangePassDTO changePassDTO) throws UserAccountNotFoundException, UnknownException {
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
            throw new UnknownException("Transaction cannot complete!");
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

    // ChangeFullname
    public void changeFullname(Integer id, String changeName)
            throws UnknownException, UserAccountNotFoundException {
        if (userRepository.findById(id).isEmpty()) {
            throw new UserAccountNotFoundException("ID: " + id + " not found. Please try another!");
        } else {
            User user = userRepository.findOneById(id);
            if (user.getLastNameChangeDate() != null) {
                long daysSinceLastChange = ChronoUnit.DAYS.between(user.getLastNameChangeDate(), LocalDateTime.now());
                if (daysSinceLastChange < 30) {
                    throw new UnknownException(
                            "You can only change your name once every 30 days. Please try again after "
                                    + (30 - daysSinceLastChange) + " days.");
                }
            }

            user.setFullname(changeName);
            user.setLastNameChangeDate(LocalDateTime.now());
            User saveName = userRepository.save(user);
            if (saveName.getId() == null) {
                throw new UnknownException("Transaction cannot complete!");
            }
        }
    }

    // ChangeProfile
    public void changeProfile(Integer id, UpdateProfileDTO updateUser, MultipartFile imageFile)
            throws UserAccountNotFoundException, UserAccountExistingException, UnknownException {

        try {
            User foundUser = userRepository.findOneById(id);
            if (foundUser == null) {
                throw new UserAccountNotFoundException("ID " + id + " not found. Please try another!");
            }

            List<User> foundExitsPhoneNumber = userRepository.findByPhoneNumber(updateUser.getPhoneNumber());
            if (foundExitsPhoneNumber.size() > 1) {
                throw new UserAccountExistingException(
                        "PhoneNumber " + updateUser.getPhoneNumber() + " already exist . Please try another!");
            }

            if (imageFile != null && !imageFile.isEmpty()) {
                String imageFileName = imageFile.getOriginalFilename();
                long imageFileSize = imageFile.getSize();
                String imageContentType = imageFile.getContentType();

                if (!isValidFileType(imageContentType)) {
                    throw new UnknownException("Invalid file type. Only image files are allowed.");
                }

                try (InputStream mediaInputStream = imageFile.getInputStream()) {
                    ObjectMetadata mediaMetadata = new ObjectMetadata();
                    mediaMetadata.setContentLength(imageFileSize);
                    mediaMetadata.setContentType(imageContentType);

                    amazonS3.putObject(bucketName, imageFileName, mediaInputStream, mediaMetadata);

                    String mediaUrl = String.format("https://travle-be.s3.ap-southeast-2.amazonaws.com/%s",
                            imageFileName);
                    foundUser.setAvatarUrl(mediaUrl);
                }
            }
            if (updateUser.getBio() != null) {
                foundUser.setBio(updateUser.getBio());
            }

            if (updateUser.getDob() != null) {
                foundUser.setDob(updateUser.getDob());
            }
            User saveUser = userRepository.save(foundUser);
            if (saveUser.getId() == null) {
                throw new UnknownException("Transaction cannot complete!");
            }
        } catch (IOException e) {
            throw new UnknownException("File handling error: " + e.getMessage());
        } catch (Exception e) {
            throw new UnknownException(e.getMessage());
        }
    }
    // Change STATUS User

    public void switchIsLockedUser(Integer id, String isLock)
            throws UserAccountNotFoundException, MethodNotValidException, UnknownException {
        User user = userRepository.findOneById(id);
        if (user == null) {
            throw new UserAccountNotFoundException("ID: " + id + " not found. Please try another!");
        }

        String[] statusUser = { "OPEN", "LOOK" };

        Boolean isCheck = isLock == null || Arrays.asList(statusUser).contains(isLock);

        if (isCheck == false) {
            throw new MethodNotValidException("Invalid status user type. Please try again !");
        }

        if (user.getIsLocked() != null && user.getIsLocked().equals(isLock)) {
            throw new UnknownException("Transaction cannot complete because old status is the same as the new status!");
        }

        user.setIsLocked(isLock);

        userRepository.save(user);
    }

    public void switchStatusUser(Integer id, String status) throws UserAccountNotFoundException, UnknownException {
        User user = userRepository.findOneById(id);
        if (user == null) {
            throw new UserAccountNotFoundException("ID: " + id + " not found. Please try another!");
        }

        String[] statusUser = { "ONLINE", "OFFLINE" };

        Boolean isCheck = status == null || Arrays.asList(statusUser).contains(status);

        if (isCheck == false) {
            throw new MethodNotValidException("Invalid status user type. Please try again !");
        }

        if (user.getStatus() != null && user.getIsLocked().equals(status)) {
            throw new UnknownException("Transaction cannot complete because old status is the same as the new status!");
        }

        user.setStatus(status);

        userRepository.save(user);
    }

    // Create Manager
    public void createManager(User user) throws UserAccountExistingException, UnknownException {
        if (!userRepository.findByEmail(user.getEmail()).isEmpty()) {
            throw new UserAccountExistingException("Email " + user.getEmail() + " already exist. Please try another!");
        }

        user.setIsLocked(AccountStatus.OPEN.name());

        PasswordEncoder passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        HashSet<String> roles = new HashSet<>();
        roles.add(Role.MANAGER.name());
        user.setRoles(roles);

        User savedUser = userRepository.save(user);
        if (savedUser.getId() == null) {
            throw new UnknownException("Transaction cannot complete!");
        }
    }

}
