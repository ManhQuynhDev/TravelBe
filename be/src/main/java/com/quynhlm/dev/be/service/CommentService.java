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
import com.quynhlm.dev.be.core.exception.UnknownException;
import com.quynhlm.dev.be.core.exception.UserAccountNotFoundException;
import com.quynhlm.dev.be.model.dto.requestDTO.CommentRequestDTO;
import com.quynhlm.dev.be.model.dto.responseDTO.CommentResponseDTO;
import com.quynhlm.dev.be.model.dto.responseDTO.ReplyResponseDTO;
import com.quynhlm.dev.be.model.dto.responseDTO.ReplyToReplyReponseDTO;
import com.quynhlm.dev.be.model.entity.Comment;
import com.quynhlm.dev.be.model.entity.Post;
import com.quynhlm.dev.be.model.entity.User;
import com.quynhlm.dev.be.repositories.CommentRepository;
import com.quynhlm.dev.be.repositories.PostRepository;
import com.quynhlm.dev.be.repositories.ReplyRepository;
import com.quynhlm.dev.be.repositories.ReplyToReplyRepositoty;
import com.quynhlm.dev.be.repositories.UserRepository;

@Service
public class CommentService {

    @Autowired
    private AmazonS3 amazonS3;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private ReplyRepository replyRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private ReplyToReplyRepositoty replyToReplyRepositoty;

    @Value("${aws.s3.bucketName}")
    private String bucketName;

    public Page<Comment> getListData(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return commentRepository.findAll(pageable);
    }

    public CommentResponseDTO insertComment(CommentRequestDTO commentRequestDTO, MultipartFile imageFile)
            throws UnknownException, PostNotFoundException, UserAccountNotFoundException {
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

            Comment comment = new Comment();
            comment.setContent(commentRequestDTO.getContent());
            comment.setUserId(commentRequestDTO.getUserId());
            if (commentRequestDTO.getPostId() != null) {
                comment.setPostId(commentRequestDTO.getPostId());
            }

            if (commentRequestDTO.getShareId() != null) {
                comment.setShareId(commentRequestDTO.getShareId());
            }

            comment.setType(commentRequestDTO.getType());

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
                    comment.setUrl(mediaUrl);
                }
            }
            comment.setCreate_time(new Timestamp(System.currentTimeMillis()).toString());
            Comment saveComment = commentRepository.save(comment);
            if (saveComment == null) {
                throw new UnknownException("Transaction cannot be completed!");
            }
            return findAnComment(saveComment.getId());
        } catch (IOException e) {
            throw new UnknownException("File handling error: " + e.getMessage());
        } catch (Exception e) {
            throw new UnknownException(e.getMessage());
        }
    }

    private boolean isValidFileType(String contentType) {
        return contentType.startsWith("image/") || contentType.startsWith("video/");
    }

    public void deleteComment(Integer id) throws CommentNotFoundException {
        Comment foundComment = commentRepository.findComment(id);
        if (foundComment == null) {
            throw new CommentNotFoundException("Comment with id " + id + " not found. Please try another!");
        }
        commentRepository.delete(foundComment);
    }

    public void updateComment(Integer id, String newContent, MultipartFile imageFile)
            throws CommentNotFoundException, UnknownException {
        Comment existingComment = commentRepository.findComment(id);
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
                existingComment.setUrl(mediaUrl);
            } catch (IOException e) {
                throw new UnknownException("File handling error: " + e.getMessage());
            }
        }
        Comment saveComment = commentRepository.save(existingComment);
        if (saveComment == null) {
            throw new UnknownException("Transaction cannot be completed!");
        }
    }

    public CommentResponseDTO findAnComment(Integer id) throws CommentNotFoundException {

        List<Object[]> results = commentRepository.findCommentWithId(id);

        if (results.isEmpty()) {
            throw new CommentNotFoundException(
                    "Id " + id + " not found or invalid data. Please try another!");
        }

        Object[] result = results.get(0);

        CommentResponseDTO comment = new CommentResponseDTO();
        comment.setCommentId(((Number) result[0]).intValue());
        comment.setOwnerId(((Number) result[1]).intValue());
        comment.setFullname((String) result[2]);
        comment.setAvatar((String) result[3]);
        comment.setContent((String) result[4]);
        comment.setPostId(result[5] != null ? ((Number) result[5]).intValue() : null);
        comment.setShareId(result[6] != null ? ((Number) result[6]).intValue() : null);
        comment.setCreate_time((String) result[7]);
        comment.setReaction_count(((Number) result[8]).intValue());
        return comment;
    }

    public Page<CommentResponseDTO> fetchCommentWithPostId(Integer postId, Integer userId, int page, int size)
            throws PostNotFoundException, UserAccountNotFoundException {
        Post foundPost = postRepository.getAnPost(postId);
        if (foundPost == null) {
            throw new PostNotFoundException("Found post with " + postId + " not found please try again");
        }

        User foundUser = userRepository.getAnUser(userId);
        if (foundUser == null) {
            throw new UserAccountNotFoundException("Found user with " + userId + " not found please try again");
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<Object[]> results = commentRepository.fetchCommentWithPostId(pageable, postId, userId);

        return results.map(row -> {
            CommentResponseDTO comment = new CommentResponseDTO();
            comment.setCommentId(((Number) row[0]).intValue());
            comment.setOwnerId(((Number) row[1]).intValue());
            comment.setFullname((String) row[2]);
            comment.setAvatar((String) row[3]);
            comment.setContent((String) row[4]);
            comment.setPostId(row[5] != null ? ((Number) row[5]).intValue() : null);
            comment.setShareId(row[6] != null ? ((Number) row[6]).intValue() : null);
            comment.setCreate_time((String) row[7]);
            comment.setReaction_count(((Number) row[8]).intValue());
            comment.setUser_reaction_type((String) row[9]);

            List<Object[]> rawResults = replyRepository.fetchReplyByCommentId(((Number) row[0]).intValue());
            List<ReplyResponseDTO> responses = rawResults.stream()
                    .map(r -> {
                        ReplyResponseDTO reply = new ReplyResponseDTO();
                        reply.setReplyId(((Number) r[0]).intValue());
                        reply.setCommentId(((Number) r[1]).intValue());
                        reply.setOwnerId(((Number) r[2]).intValue());
                        reply.setFullname((String) r[3]);
                        reply.setAvatar((String) r[4]);
                        reply.setContent((String) r[5]);
                        reply.setCreate_time((String) r[6]);
                        reply.setReaction_count(((Number) r[7]).intValue());
                        reply.setIsAuthor(foundPost.getUser_id() == ((Number) r[2]).intValue());

                        List<Object[]> rawReplys = replyToReplyRepositoty
                                .fetchReplyToReplyByReplyId(((Number) r[0]).intValue());

                        List<ReplyToReplyReponseDTO> responseReply = rawReplys.stream()
                                .map(result -> {
                                    ReplyToReplyReponseDTO reply_to_reply = new ReplyToReplyReponseDTO();
                                    reply_to_reply.setId(((Number) result[0]).intValue());
                                    reply_to_reply.setReplyId(((Number) result[1]).intValue());
                                    reply_to_reply.setOwnerId(((Number) result[2]).intValue());
                                    reply_to_reply.setFullname((String) result[3]);
                                    reply_to_reply.setAvatar((String) result[4]);
                                    reply_to_reply.setContent((String) result[5]);
                                    reply_to_reply.setCreate_time((String) result[6]);

                                    reply_to_reply
                                            .setIsAuthor(foundPost.getUser_id() == ((Number) result[2]).intValue());

                                    return reply_to_reply;
                                }).collect(Collectors.toList());

                        reply.setReplys(responseReply);

                        return reply;
                    })
                    .collect(Collectors.toList());

            comment.setIsAuthor(foundPost.getUser_id() == ((Number) row[1]).intValue());
            comment.setReplys(responses);

            return comment;
        });
    }

    public Page<CommentResponseDTO> fetchCommentWithShareId(Integer postId, Integer userId, int page, int size)
            throws PostNotFoundException, UserAccountNotFoundException {

        Post foundPost = postRepository.getAnPost(postId);
        if (foundPost == null) {
            throw new PostNotFoundException("Post with ID " + postId + " not found. Please try again.");
        }

        User foundUser = userRepository.getAnUser(userId);
        if (foundUser == null) {
            throw new UserAccountNotFoundException("Found user with " + userId + " not found please try again");
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<Object[]> results = commentRepository.fetchCommentWithPostId(pageable, postId, userId);

        return results.map(row -> {
            CommentResponseDTO comment = new CommentResponseDTO();
            comment.setCommentId(((Number) row[0]).intValue());
            comment.setOwnerId(((Number) row[1]).intValue());
            comment.setFullname((String) row[2]);
            comment.setAvatar((String) row[3]);
            comment.setContent((String) row[4]);
            comment.setPostId(row[5] != null ? ((Number) row[5]).intValue() : null);
            comment.setShareId(row[6] != null ? ((Number) row[6]).intValue() : null);
            comment.setCreate_time((String) row[7]);
            comment.setReaction_count(((Number) row[8]).intValue());
            comment.setUser_reaction_type((String) row[9]);

            List<Object[]> rawResults = replyRepository.fetchReplyByCommentId(((Number) row[0]).intValue());
            List<ReplyResponseDTO> responses = rawResults.stream()
                    .map(r -> {
                        ReplyResponseDTO reply = new ReplyResponseDTO();
                        reply.setReplyId(((Number) r[0]).intValue());
                        reply.setCommentId(((Number) r[1]).intValue());
                        reply.setOwnerId(((Number) r[2]).intValue());
                        reply.setFullname((String) r[3]);
                        reply.setAvatar((String) r[4]);
                        reply.setContent((String) r[5]);
                        reply.setCreate_time((String) r[6]);
                        reply.setReaction_count(((Number) r[7]).intValue());

                        // Fetch reply-to-reply data
                        List<Object[]> rawReplys = replyToReplyRepositoty
                                .fetchReplyToReplyByReplyId(((Number) r[0]).intValue()); // Sửa tham số truy vấn

                        List<ReplyToReplyReponseDTO> responseReply = rawReplys.stream()
                                .map(result -> {
                                    ReplyToReplyReponseDTO reply_to_reply = new ReplyToReplyReponseDTO();
                                    reply_to_reply.setId(((Number) result[0]).intValue());
                                    reply_to_reply.setReplyId(((Number) result[1]).intValue());
                                    reply_to_reply.setOwnerId(((Number) result[2]).intValue());
                                    reply_to_reply.setFullname((String) result[3]);
                                    reply_to_reply.setAvatar((String) result[4]);
                                    reply_to_reply.setContent((String) result[5]);
                                    reply_to_reply.setCreate_time((String) result[6]);

                                    return reply_to_reply;
                                }).collect(Collectors.toList());

                        reply.setReplys(responseReply);

                        return reply;
                    })
                    .collect(Collectors.toList());

            comment.setIsAuthor(foundPost.getUser_id() == ((Number) row[1]).intValue());
            comment.setReplys(responses);

            return comment;
        });
    }

}