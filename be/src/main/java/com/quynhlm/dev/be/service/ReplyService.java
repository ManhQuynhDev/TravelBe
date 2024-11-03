package com.quynhlm.dev.be.service;

import java.io.InputStream;
import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.quynhlm.dev.be.core.exception.CommentNotFoundException;
import com.quynhlm.dev.be.core.exception.ReplyNotFoundException;
import com.quynhlm.dev.be.core.exception.UnknownException;
import com.quynhlm.dev.be.model.entity.Comment;
import com.quynhlm.dev.be.model.entity.Reply;
import com.quynhlm.dev.be.repositories.CommentRepository;
import com.quynhlm.dev.be.repositories.ReplyRepository;
import java.util.List;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReplyService {

    @Autowired
    private AmazonS3 amazonS3;

    @Value("${aws.s3.bucketName}")
    private String bucketName;

    @Autowired
    private ReplyRepository replyRepository;

    @Autowired
    private CommentRepository commentRepository;

    // Insert reply
    public void insertReply(Reply reply, MultipartFile imageFile) throws UnknownException {
        try {
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
        Reply foundReply = replyRepository.findReply(id);
        if (foundReply == null) {
            throw new ReplyNotFoundException("Reply with id " + id + " not found. Please try another!");
        }
        replyRepository.delete(foundReply);
    }

    public void updateReply(Integer id, String newContent, MultipartFile imageFile)
            throws ReplyNotFoundException, UnknownException {
        Reply existingReply = replyRepository.findReply(id);
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

    public void isSuccess(Reply reply) {
        Reply saveReply = replyRepository.save(reply);
        if (saveReply.getId() == null) {
            throw new UnknownException("Transaction cannot complete!");
        }
    }

    public List<Reply> findReplyByCommentId(Integer comment_id) throws CommentNotFoundException {
        Comment foundComment = commentRepository.findComment(comment_id);
        if (foundComment == null) {
            throw new CommentNotFoundException("Comment with id " + comment_id + " not found.");
        }
        return replyRepository.findReplyByCommentId(comment_id);
    }
}