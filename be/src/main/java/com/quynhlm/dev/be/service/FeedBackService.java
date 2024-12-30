package com.quynhlm.dev.be.service;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.quynhlm.dev.be.core.exception.CommentNotFoundException;
import com.quynhlm.dev.be.core.exception.PostNotFoundException;
import com.quynhlm.dev.be.core.exception.ShareNotFoundException;
import com.quynhlm.dev.be.core.exception.UnknownException;
import com.quynhlm.dev.be.core.exception.UserAccountNotFoundException;
import com.quynhlm.dev.be.model.dto.requestDTO.FeedBackRequestDTO;
import com.quynhlm.dev.be.model.dto.responseDTO.FeedBackResponseDTO;
import com.quynhlm.dev.be.model.entity.Feedback;
import com.quynhlm.dev.be.model.entity.Post;
import com.quynhlm.dev.be.model.entity.User;
import com.quynhlm.dev.be.repositories.FeedbackRepository;
import com.quynhlm.dev.be.repositories.PostRepository;
import com.quynhlm.dev.be.repositories.UserRepository;

@Service
public class FeedBackService {
    @Autowired
    private FeedbackRepository feedbackRepository;

    @Autowired
    private AmazonS3 amazonS3;

    @Value("${aws.s3.bucketName}")
    private String bucketName;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    public FeedBackResponseDTO insertComment(FeedBackRequestDTO commentRequestDTO, MultipartFile imageFile)
            throws UnknownException, ShareNotFoundException, PostNotFoundException, UserAccountNotFoundException {
        try {

            Post foundPost = postRepository.getAnPost(commentRequestDTO.getPostId());
            if (foundPost == null) {
                throw new PostNotFoundException(
                        "Found post with " + commentRequestDTO.getPostId() + " not found please try again");
            }

            User foundUser = userRepository.getAnUser(commentRequestDTO.getUserId());
            if (foundUser == null) {
                throw new UserAccountNotFoundException(
                        "Found user with " + commentRequestDTO.getUserId() + " not found please try again");
            }

            Feedback comment = new Feedback();
            comment.setContent(commentRequestDTO.getContent());
            comment.setUserId(commentRequestDTO.getUserId());
            comment.setPostId(commentRequestDTO.getPostId());
            comment.setReplyToId(commentRequestDTO.getReplyToId() == null ? null : commentRequestDTO.getReplyToId());
            comment.setIsReply(commentRequestDTO.getReplyToId() == null ? 0 : 1);
            comment.setDelFlag(0);

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

                    comment.setMediaUrl(mediaUrl);
                }
            }
            comment.setCreateTime(new Timestamp(System.currentTimeMillis()).toString());
            Feedback saveComment = feedbackRepository.save(comment);
            if (saveComment == null) {
                throw new UnknownException("Transaction cannot be completed!");
            }
            return findAnComment(saveComment.getId());
        } catch (IOException e) {
            throw new UnknownException("File handling error: " + e.getMessage());
        }
    }

    private boolean isValidFileType(String contentType) {
        return contentType.startsWith("image/") || contentType.startsWith("video/");
    }

    public void deleteComment(Integer id) throws CommentNotFoundException {
        Feedback foundComment = feedbackRepository.findComment(id);
        if (foundComment == null) {
            throw new CommentNotFoundException("Comment with id " + id + " not found. Please try another!");
        }

        feedbackRepository.softDeleteFeedbackAndReplies(id);
    }

    public FeedBackResponseDTO findAnComment(Integer id) throws CommentNotFoundException {
        List<Object[]> results = feedbackRepository.findCommentWithId(id);

        if (results.isEmpty()) {
            throw new CommentNotFoundException(
                    "Id " + id + " not found or invalid data. Please try another!");
        }

        Object[] row = results.get(0);

        FeedBackResponseDTO comment = new FeedBackResponseDTO();
        comment.setCommentId(((Number) row[0]).intValue());
        comment.setOwnerId(((Number) row[1]).intValue());
        comment.setFullname((String) row[2]);
        comment.setAvatar((String) row[3]);
        comment.setContent((String) row[4]);
        comment.setMediaUrl((String) row[5]);
        comment.setPostId(((Number) row[6]).intValue());
        comment.setIs_reply(row[7] != null ? ((Number) row[7]).intValue() : null);
        comment.setReply_to_id(row[8] != null ? ((Number) row[8]).intValue() : null);
        comment.setCreate_time((String) row[9]);
        Post foundPost = postRepository.getAnPost(((Number) row[6]).intValue());

        if ((String) row[5] == null) {
            comment.setMediaType(null);
        } else {
            comment.setMediaType(
                    (String) row[5] != null && ((String) row[5]).matches(".*\\.(jpg|jpeg|png|gif|webp)$")
                            ? "IMAGE"
                            : "VIDEO");
        }

        List<Object[]> rawResults = feedbackRepository.findReplyWithCommentId(((Number) row[0]).intValue());
        List<FeedBackResponseDTO> responses = rawResults.stream()
                .map(r -> {
                    FeedBackResponseDTO reply = new FeedBackResponseDTO();
                    reply.setCommentId(((Number) row[0]).intValue());
                    reply.setOwnerId(((Number) row[1]).intValue());
                    reply.setFullname((String) row[2]);
                    reply.setAvatar((String) row[3]);
                    reply.setContent((String) row[4]);
                    reply.setMediaUrl((String) row[5]);
                    reply.setPostId(((Number) row[6]).intValue());
                    reply.setIs_reply(row[7] != null ? ((Number) row[7]).intValue() : null);
                    reply.setReply_to_id(row[8] != null ? ((Number) row[8]).intValue() : null);
                    reply.setCreate_time((String) row[9]);
                    reply.setIsAuthor(foundPost.getUser_id() == ((Number) r[1]).intValue());
                    return reply;
                })
                .collect(Collectors.toList());

        comment.setIsAuthor(foundPost.getUser_id() == ((Number) row[1]).intValue());
        comment.setReplys(responses);

        return comment;
    }

    public void updateComment(Integer id, String newContent, MultipartFile imageFile)
            throws CommentNotFoundException, UnknownException {
        Feedback existingComment = feedbackRepository.findComment(id);
        if (existingComment == null) {
            throw new CommentNotFoundException("Comment with id " + id + " not found.");
        }
        existingComment.setContent(newContent);
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
                existingComment.setMediaUrl(mediaUrl);
            } catch (IOException e) {
                throw new UnknownException("File handling error: " + e.getMessage());
            }
        }
        Feedback saveComment = feedbackRepository.save(existingComment);
        if (saveComment == null) {
            throw new UnknownException("Transaction cannot be completed!");
        }
    }

    public Page<FeedBackResponseDTO> fetchCommentWithPostId(Integer postId, int page, int size)
            throws PostNotFoundException, UserAccountNotFoundException {

        Post foundPost = postRepository.getAnPost(postId);
        if (foundPost == null) {
            throw new PostNotFoundException("Found post with " + postId + " not found please try again");
        }

        // User foundUser = userRepository.getAnUser(userId);
        // if (foundUser == null) {
        // throw new UserAccountNotFoundException("Found user with " + userId + " not
        // found please try again");
        // }

        Pageable pageable = PageRequest.of(page, size);
        Page<Object[]> results = feedbackRepository.findCommentWithPostId(postId, pageable);
        return results.map(row -> {
            FeedBackResponseDTO comment = new FeedBackResponseDTO();
            comment.setCommentId(((Number) row[0]).intValue());
            comment.setOwnerId(((Number) row[1]).intValue());
            comment.setFullname((String) row[2]);
            comment.setAvatar((String) row[3]);
            comment.setContent((String) row[4]);
            comment.setMediaUrl((String) row[5]);
            comment.setPostId(((Number) row[6]).intValue());
            comment.setIs_reply(row[7] != null ? ((Number) row[7]).intValue() : null);
            comment.setReply_to_id(row[8] != null ? ((Number) row[8]).intValue() : null);
            comment.setCreate_time((String) row[9]);

            if ((String) row[5] == null) {
                comment.setMediaType(null);
            } else {
                comment.setMediaType(
                        (String) row[5] != null && ((String) row[5]).matches(".*\\.(jpg|jpeg|png|gif|webp)$")
                                ? "IMAGE"
                                : "VIDEO");
            }

            List<Object[]> rawResults = feedbackRepository.findReplyWithCommentId(((Number) row[0]).intValue());
            List<FeedBackResponseDTO> responses = rawResults.stream()
                    .map(r -> {
                        FeedBackResponseDTO reply = new FeedBackResponseDTO();
                        reply.setCommentId(((Number) row[0]).intValue());
                        reply.setOwnerId(((Number) row[1]).intValue());
                        reply.setFullname((String) row[2]);
                        reply.setAvatar((String) row[3]);
                        reply.setContent((String) row[4]);
                        reply.setMediaUrl((String) row[5]);
                        reply.setPostId(((Number) row[6]).intValue());
                        reply.setIs_reply(row[7] != null ? ((Number) row[7]).intValue() : null);
                        reply.setReply_to_id(row[8] != null ? ((Number) row[8]).intValue() : null);
                        reply.setCreate_time((String) row[9]);
                        reply.setIsAuthor(foundPost.getUser_id() == ((Number) r[1]).intValue());
                        return reply;
                    })
                    .collect(Collectors.toList());

            comment.setIsAuthor(foundPost.getUser_id() == ((Number) row[1]).intValue());
            comment.setReplys(responses);

            return comment;
        });
    }
}
