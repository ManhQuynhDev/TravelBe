package com.quynhlm.dev.be.service;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

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
import com.quynhlm.dev.be.model.dto.requestDTO.FeedBackRequestDTO;
import com.quynhlm.dev.be.model.dto.responseDTO.CommentResponseDTO;
import com.quynhlm.dev.be.model.dto.responseDTO.ReactionCountDTO;
import com.quynhlm.dev.be.model.entity.Comment;
import com.quynhlm.dev.be.model.entity.Post;
import com.quynhlm.dev.be.model.entity.User;
import com.quynhlm.dev.be.repositories.CommentReactionRepository;
import com.quynhlm.dev.be.repositories.CommentRepository;
import com.quynhlm.dev.be.repositories.PostRepository;
import com.quynhlm.dev.be.repositories.UserRepository;

import net.coobird.thumbnailator.Thumbnails;

@Service
public class CommentService {
    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private AmazonS3 amazonS3;

    @Value("${aws.s3.bucketName}")
    private String bucketName;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CommentReactionRepository commentReactionRepository;

    public CommentResponseDTO insertComment(FeedBackRequestDTO commentRequestDTO, MultipartFile imageFile)
            throws UnknownException, PostNotFoundException, CommentNotFoundException, UserAccountNotFoundException {
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

            if (commentRequestDTO.getReplyToId() != null) {
                Comment foundComment = commentRepository.findComment(commentRequestDTO.getReplyToId());
                if (foundComment == null) {
                    throw new CommentNotFoundException(
                            "Found comment with " + commentRequestDTO.getReplyToId() + " not found please try again");
                }
            }

            Comment comment = new Comment();
            comment.setContent(commentRequestDTO.getContent());
            comment.setUserId(commentRequestDTO.getUserId());
            comment.setPostId(commentRequestDTO.getPostId());
            comment.setReplyToId(commentRequestDTO.getReplyToId() == null ? null : commentRequestDTO.getReplyToId());
            comment.setIsReply(commentRequestDTO.getReplyToId() == null ? 0 : 1);
            comment.setDelFlag(0);

            if (imageFile != null && !imageFile.isEmpty()) {
                String mediaUrl = uploadMediaToS3(imageFile);
                comment.setMediaUrl(mediaUrl);
            }

            comment.setCreateTime(new Timestamp(System.currentTimeMillis()).toString());
            Comment saveComment = commentRepository.save(comment);
            if (saveComment == null) {
                throw new UnknownException("Transaction cannot be completed!");
            }
            return findAnComment(saveComment.getId(), saveComment.getUserId());
        } catch (IOException e) {
            throw new UnknownException("File handling error: " + e.getMessage());
        }
    }

    private String uploadMediaToS3(MultipartFile file) throws IOException, UnknownException {
        String fileName = file.getOriginalFilename();
        String contentType = file.getContentType();

        if (!isValidFileType(contentType)) {
            throw new UnknownException("Invalid file type. Only image and video files are allowed.");
        }

        try (InputStream inputStream = file.getInputStream()) {
            if (contentType.startsWith("image/")) {
                BufferedImage originalImage = ImageIO.read(inputStream);

                BufferedImage resizedImage = Thumbnails.of(originalImage)
                        .scale(0.5)
                        .outputQuality(0.1)
                        .asBufferedImage();

                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                ImageIO.write(resizedImage, "jpg", outputStream);
                InputStream resizedInputStream = new ByteArrayInputStream(outputStream.toByteArray());

                ObjectMetadata metadata = new ObjectMetadata();
                metadata.setContentLength(outputStream.size());
                metadata.setContentType(contentType);

                amazonS3.putObject(bucketName, fileName, resizedInputStream, metadata);

            } else if (contentType.startsWith("video/")) {

                ObjectMetadata metadata = new ObjectMetadata();
                metadata.setContentLength(file.getSize());
                metadata.setContentType(contentType);

                amazonS3.putObject(bucketName, fileName, inputStream, metadata);
            }

            String mediaUrl = String.format("https://travle-be.s3.ap-southeast-2.amazonaws.com/%s",
                    fileName);
            return mediaUrl;
        } catch (Exception e) {
            throw new UnknownException("Error uploading file to S3: " + e.getMessage());
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

        commentRepository.softDeleteFeedbackAndReplies(id);
    }

    public CommentResponseDTO findAnComment(Integer id, Integer userId) throws CommentNotFoundException {
        List<Object[]> results = commentRepository.findCommentWithId(id, userId);

        if (results.isEmpty()) {
            throw new CommentNotFoundException(
                    "Id " + id + " not found or invalid data. Please try another!");
        }

        Object[] row = results.get(0);

        CommentResponseDTO comment = new CommentResponseDTO();
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
        comment.setReaction_count(((Number) row[10]).intValue());
        comment.setUser_reaction_type((String) row[11]);

        List<Object[]> reactions = commentReactionRepository.findTopReactions(((Number) row[0]).intValue());

        if (!reactions.isEmpty()) {
            List<ReactionCountDTO> reactionStatisticsList = new ArrayList<>();

            for (Object[] result : reactions) {
                ReactionCountDTO reactionStatisticsDTO = new ReactionCountDTO();
                reactionStatisticsDTO.setType((String) result[0]);
                reactionStatisticsDTO.setReactionCount(((Number) result[1]).intValue());
                reactionStatisticsList.add(reactionStatisticsDTO);
            }
            comment.setReactionStatistics(reactionStatisticsList);
        } else {
            List<ReactionCountDTO> newLis = new ArrayList<>();
            comment.setReactionStatistics(newLis);
        }

        Post foundPost = postRepository.getAnPost(((Number) row[6]).intValue());

        if ((String) row[5] == null) {
            comment.setMediaType(null);
        } else {
            comment.setMediaType(
                    (String) row[5] != null && ((String) row[5]).matches(".*\\.(jpg|jpeg|png|gif|webp)$")
                            ? "IMAGE"
                            : "VIDEO");
        }

        List<Object[]> rawResults = commentRepository.findReplyWithCommentId(((Number) row[0]).intValue(), userId);
        List<CommentResponseDTO> responses = rawResults.stream()
                .map(r -> {
                    CommentResponseDTO reply = new CommentResponseDTO();
                    reply.setCommentId(((Number) r[0]).intValue());
                    reply.setOwnerId(((Number) r[1]).intValue());
                    reply.setFullname((String) r[2]);
                    reply.setAvatar((String) r[3]);
                    reply.setContent((String) r[4]);
                    reply.setMediaUrl((String) r[5]);
                    reply.setPostId(((Number) r[6]).intValue());
                    reply.setIs_reply(r[7] != null ? ((Number) r[7]).intValue() : null);
                    reply.setReply_to_id(r[8] != null ? ((Number) r[8]).intValue() : null);
                    reply.setCreate_time((String) r[9]);
                    reply.setReaction_count(((Number) r[10]).intValue());
                    reply.setUser_reaction_type((String) r[11]);
                    reply.setIsAuthor(foundPost.getUser_id() == ((Number) r[1]).intValue());

                    List<Object[]> reactionReply = commentReactionRepository
                            .findTopReactions(((Number) r[0]).intValue());

                    if (!reactionReply.isEmpty()) {
                        List<ReactionCountDTO> reactionStatisticsList = new ArrayList<>();

                        for (Object[] result : reactionReply) {
                            ReactionCountDTO reactionStatisticsDTO = new ReactionCountDTO();
                            reactionStatisticsDTO.setType((String) result[0]);
                            reactionStatisticsDTO.setReactionCount(((Number) result[1]).intValue());
                            reactionStatisticsList.add(reactionStatisticsDTO);
                        }
                        reply.setReactionStatistics(reactionStatisticsList);
                    } else {
                        List<ReactionCountDTO> newLis = new ArrayList<>();
                        reply.setReactionStatistics(newLis);
                    }

                    List<Object[]> rawResultsReply = commentRepository.findReplyWithCommentId(
                            ((Number) r[0]).intValue(),
                            userId);

                    List<CommentResponseDTO> responsesReply = rawResultsReply.stream()
                            .map(nestedReply -> {
                                if (nestedReply == null || nestedReply.length < 12 || nestedReply[0] == null) {
                                    return null;
                                }
                                CommentResponseDTO reply_to_reply = new CommentResponseDTO();
                                reply_to_reply.setCommentId(
                                        nestedReply[0] != null ? ((Number) nestedReply[0]).intValue() : null);
                                reply_to_reply.setOwnerId(((Number) nestedReply[1]).intValue());
                                reply_to_reply.setFullname((String) nestedReply[2]);
                                reply_to_reply.setAvatar((String) nestedReply[3]);
                                reply_to_reply.setContent((String) nestedReply[4]);
                                reply_to_reply.setMediaUrl((String) nestedReply[5]);
                                reply_to_reply.setPostId(((Number) nestedReply[6]).intValue());
                                reply_to_reply.setIs_reply(
                                        nestedReply[7] != null ? ((Number) nestedReply[7]).intValue() : null);
                                reply_to_reply.setReply_to_id(
                                        nestedReply[8] != null ? ((Number) nestedReply[8]).intValue() : null);
                                reply_to_reply.setCreate_time((String) nestedReply[9]);
                                reply_to_reply.setReaction_count(((Number) nestedReply[10]).intValue());
                                reply_to_reply.setUser_reaction_type((String) nestedReply[11]);
                                reply_to_reply
                                        .setIsAuthor(foundPost.getUser_id() == ((Number) nestedReply[1]).intValue());

                                List<Object[]> reactionReplyToReply = commentReactionRepository
                                        .findTopReactions(((Number) nestedReply[0]).intValue());

                                if (!reactionReplyToReply.isEmpty()) {
                                    List<ReactionCountDTO> reactionStatisticsList = new ArrayList<>();

                                    for (Object[] result : reactionReplyToReply) {
                                        ReactionCountDTO reactionStatisticsDTO = new ReactionCountDTO();
                                        reactionStatisticsDTO.setType((String) result[0]);
                                        reactionStatisticsDTO.setReactionCount(((Number) result[1]).intValue());
                                        reactionStatisticsList.add(reactionStatisticsDTO);
                                    }
                                    reply_to_reply.setReactionStatistics(reactionStatisticsList);
                                } else {
                                    List<ReactionCountDTO> newLis = new ArrayList<>();
                                    reply_to_reply.setReactionStatistics(newLis);
                                }

                                return reply_to_reply;
                            })
                            .collect(Collectors.toList());

                    reply.setReplys(responsesReply);

                    return reply;
                })
                .collect(Collectors.toList());

        comment.setIsAuthor(foundPost.getUser_id() == ((Number) row[1]).intValue());
        comment.setReplys(responses);

        return comment;
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
                existingComment.setMediaUrl(mediaUrl);
            } catch (IOException e) {
                throw new UnknownException("File handling error: " + e.getMessage());
            }
        }
        Comment saveComment = commentRepository.save(existingComment);
        if (saveComment == null) {
            throw new UnknownException("Transaction cannot be completed!");
        }
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
        Page<Object[]> results = commentRepository.findCommentWithPostId(postId, userId, pageable);
        return results.map(row -> {
            CommentResponseDTO comment = new CommentResponseDTO();
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
            comment.setReaction_count(((Number) row[10]).intValue());
            comment.setUser_reaction_type((String) row[11]);

            List<Object[]> reactions = commentReactionRepository.findTopReactions(((Number) row[0]).intValue());

            if (!reactions.isEmpty()) {
                List<ReactionCountDTO> reactionStatisticsList = new ArrayList<>();

                for (Object[] result : reactions) {
                    ReactionCountDTO reactionStatisticsDTO = new ReactionCountDTO();
                    reactionStatisticsDTO.setType((String) result[0]);
                    reactionStatisticsDTO.setReactionCount(((Number) result[1]).intValue());
                    reactionStatisticsList.add(reactionStatisticsDTO);
                }
                comment.setReactionStatistics(reactionStatisticsList);
            } else {
                List<ReactionCountDTO> newLis = new ArrayList<>();
                comment.setReactionStatistics(newLis);
            }

            if ((String) row[5] == null) {
                comment.setMediaType(null);
            } else {
                comment.setMediaType(
                        (String) row[5] != null && ((String) row[5]).matches(".*\\.(jpg|jpeg|png|gif|webp)$")
                                ? "IMAGE"
                                : "VIDEO");
            }

            List<Object[]> rawResults = commentRepository.findReplyWithCommentId(((Number) row[0]).intValue(), userId);

            List<CommentResponseDTO> responses = rawResults.stream()
                    .map(r -> {
                        CommentResponseDTO reply = new CommentResponseDTO();
                        reply.setCommentId(((Number) r[0]).intValue());
                        reply.setOwnerId(((Number) r[1]).intValue());
                        reply.setFullname((String) r[2]);
                        reply.setAvatar((String) r[3]);
                        reply.setContent((String) r[4]);
                        reply.setMediaUrl((String) r[5]);
                        reply.setPostId(((Number) r[6]).intValue());
                        reply.setIs_reply(r[7] != null ? ((Number) r[7]).intValue() : null);
                        reply.setReply_to_id(r[8] != null ? ((Number) r[8]).intValue() : null);
                        reply.setCreate_time((String) r[9]);
                        reply.setReaction_count(((Number) r[10]).intValue());
                        reply.setUser_reaction_type((String) r[11]);
                        reply.setIsAuthor(foundPost.getUser_id() == ((Number) r[1]).intValue());

                        List<Object[]> reactionReply = commentReactionRepository
                                .findTopReactions(((Number) r[0]).intValue());

                        if (!reactionReply.isEmpty()) {
                            List<ReactionCountDTO> reactionStatisticsList = new ArrayList<>();

                            for (Object[] result : reactionReply) {
                                ReactionCountDTO reactionStatisticsDTO = new ReactionCountDTO();
                                reactionStatisticsDTO.setType((String) result[0]);
                                reactionStatisticsDTO.setReactionCount(((Number) result[1]).intValue());
                                reactionStatisticsList.add(reactionStatisticsDTO);
                            }
                            reply.setReactionStatistics(reactionStatisticsList);
                        } else {
                            List<ReactionCountDTO> newLis = new ArrayList<>();
                            reply.setReactionStatistics(newLis);
                        }

                        List<Object[]> rawResultsReply = commentRepository.findReplyWithCommentId(
                                ((Number) r[0]).intValue(),
                                userId);

                        List<CommentResponseDTO> responsesReply = rawResultsReply.stream()
                                .map(nestedReply -> {
                                    if (nestedReply == null || nestedReply.length < 12 || nestedReply[0] == null) {
                                        return null;
                                    }
                                    CommentResponseDTO reply_to_reply = new CommentResponseDTO();
                                    reply_to_reply.setCommentId(((Number) nestedReply[0]).intValue());
                                    reply_to_reply.setOwnerId(((Number) nestedReply[1]).intValue());
                                    reply_to_reply.setFullname((String) nestedReply[2]);
                                    reply_to_reply.setAvatar((String) nestedReply[3]);
                                    reply_to_reply.setContent((String) nestedReply[4]);
                                    reply_to_reply.setMediaUrl((String) nestedReply[5]);
                                    reply_to_reply.setPostId(((Number) nestedReply[6]).intValue());
                                    reply_to_reply.setIs_reply(
                                            nestedReply[7] != null ? ((Number) nestedReply[7]).intValue() : null);
                                    reply_to_reply.setReply_to_id(
                                            nestedReply[8] != null ? ((Number) nestedReply[8]).intValue() : null);
                                    reply_to_reply.setCreate_time((String) nestedReply[9]);
                                    reply_to_reply.setReaction_count(((Number) nestedReply[10]).intValue());
                                    reply_to_reply.setUser_reaction_type((String) nestedReply[11]);
                                    reply_to_reply
                                            .setIsAuthor(
                                                    foundPost.getUser_id() == ((Number) nestedReply[1]).intValue());

                                    List<Object[]> reactionReplyToReply = commentReactionRepository
                                            .findTopReactions(((Number) nestedReply[0]).intValue());

                                    if (!reactionReplyToReply.isEmpty()) {
                                        List<ReactionCountDTO> reactionStatisticsList = new ArrayList<>();

                                        for (Object[] result : reactionReplyToReply) {
                                            ReactionCountDTO reactionStatisticsDTO = new ReactionCountDTO();
                                            reactionStatisticsDTO.setType((String) result[0]);
                                            reactionStatisticsDTO.setReactionCount(((Number) result[1]).intValue());
                                            reactionStatisticsList.add(reactionStatisticsDTO);
                                        }
                                        reply_to_reply.setReactionStatistics(reactionStatisticsList);
                                    } else {
                                        List<ReactionCountDTO> newLis = new ArrayList<>();
                                        reply_to_reply.setReactionStatistics(newLis);
                                    }

                                    return reply_to_reply;
                                })
                                .collect(Collectors.toList());

                        reply.setReplys(responsesReply);

                        return reply;
                    })
                    .collect(Collectors.toList());

            comment.setIsAuthor(foundPost.getUser_id() == ((Number) row[1]).intValue());
            comment.setReplys(responses);

            return comment;
        });
    }
}
