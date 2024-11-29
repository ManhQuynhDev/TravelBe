package com.quynhlm.dev.be.service;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.quynhlm.dev.be.core.exception.ReplyNotFoundException;
import com.quynhlm.dev.be.core.exception.UnknownException;
import com.quynhlm.dev.be.core.exception.UserAccountNotFoundException;
import com.quynhlm.dev.be.model.dto.requestDTO.ReplyToReplyRequestDTO;
import com.quynhlm.dev.be.model.entity.Reply;
import com.quynhlm.dev.be.model.entity.ReplyToReply;
import com.quynhlm.dev.be.model.entity.User;
import com.quynhlm.dev.be.repositories.ReplyRepository;
import com.quynhlm.dev.be.repositories.ReplyToReplyRepositoty;
import com.quynhlm.dev.be.repositories.UserRepository;


@Service
public class ReplyToReplyService {

    @Autowired
    private AmazonS3 amazonS3;

    @Value("${aws.s3.bucketName}")
    private String bucketName;

    @Autowired
    private ReplyToReplyRepositoty replyToReplyRepositoty;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReplyRepository replyRepository;


    public ReplyToReply insertReply(ReplyToReplyRequestDTO replyRequestDTO, MultipartFile imageFile)
            throws UserAccountNotFoundException, ReplyNotFoundException, UnknownException {
        try {

            User user = userRepository.getAnUser(replyRequestDTO.getUserId());
            if (user == null) {
                throw new UserAccountNotFoundException(
                        "Found user with " + replyRequestDTO.getUserId() + " not found , please try with other id");
            }

            Reply foundReply = replyRepository.findReply(replyRequestDTO.getReplyId());
            if (foundReply == null) {
                throw new ReplyNotFoundException(
                        "Found comment with " + replyRequestDTO.getReplyId()
                                + " not found , please try with other id");
            }

            ReplyToReply reply = new ReplyToReply();
            reply.setReplyId(replyRequestDTO.getReplyId());
            reply.setUserId(replyRequestDTO.getUserId());
            reply.setContent(replyRequestDTO.getContent());
            reply.setCreate_time(new Timestamp(System.currentTimeMillis()).toString());

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
                    reply.setUrl(mediaUrl);
                }
            }

            isSuccess(reply);
            return reply;
        } catch (IOException e) {
            throw new UnknownException("File handling error: " + e.getMessage());
        } catch (Exception e) {
            throw new UnknownException(e.getMessage());
        }
    }

    private boolean isValidFileType(String contentType) {
        return contentType.startsWith("image/") || contentType.startsWith("video/");
    }

    public void deleteReply(Integer id) throws ReplyNotFoundException {
        ReplyToReply foundReply = replyToReplyRepositoty.findReplyResult(id);
        if (foundReply == null) {
            throw new ReplyNotFoundException("Reply with id " + id + " not found. Please try another!");
        }
        replyToReplyRepositoty.delete(foundReply);
    }

    public void updateReply(Integer id, String newContent, MultipartFile imageFile)
            throws ReplyNotFoundException, UnknownException {
        ReplyToReply existingReply = replyToReplyRepositoty.findReplyResult(id);
        if (existingReply == null) {
            throw new ReplyNotFoundException("Reply with id " + id + " not found.");
        }
        existingReply.setContent(newContent);
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
                String mediaUrl = String.format("https://travle-be.s3.ap-southeast-2.amazonaws.com/%s", imageFileName);
                existingReply.setUrl(mediaUrl);
            } catch (IOException e) {
                throw new UnknownException("File handling error: " + e.getMessage());
            }
        }
        isSuccess(existingReply);
    }

    public void isSuccess(ReplyToReply reply) {
        ReplyToReply saveReply = replyToReplyRepositoty.save(reply);
        if (saveReply.getId() == null) {
            throw new UnknownException("Transaction cannot complete!");
        }
    }
}
