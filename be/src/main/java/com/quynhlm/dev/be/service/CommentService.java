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
import com.quynhlm.dev.be.core.exception.UnknownException;
import com.quynhlm.dev.be.model.dto.requestDTO.CommentRequestDTO;
import com.quynhlm.dev.be.model.dto.responseDTO.CommentResponseDTO;
import com.quynhlm.dev.be.model.dto.responseDTO.ReplyResponseDTO;
import com.quynhlm.dev.be.model.entity.Comment;
import com.quynhlm.dev.be.repositories.CommentRepository;
import com.quynhlm.dev.be.repositories.ReplyRepository;

@Service
public class CommentService {

    @Autowired
    private AmazonS3 amazonS3;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private ReplyRepository replyRepository;

    @Value("${aws.s3.bucketName}")
    private String bucketName;

    public Page<Comment> getListData(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return commentRepository.findAll(pageable);
    }

    public void insertComment(CommentRequestDTO commentRequestDTO, MultipartFile imageFile) throws UnknownException {
        try {

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
            commentRepository.save(comment);
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
        commentRepository.save(existingComment);
    }

    public Comment findAnComment(Integer id) throws CommentNotFoundException {
        Comment comment = commentRepository.findComment(id);
        if (comment == null) {
            throw new CommentNotFoundException("Id " + id + " not found . Please try another!");
        }
        return comment;
    }

    public Page<CommentResponseDTO> fetchCommentWithPostId(Integer postId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Object[]> results = commentRepository.fetchCommentWithPostId(pageable, postId);

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
            comment.setReply_count(((Number) row[9]).intValue());

            List<Object[]> rawResults = replyRepository.fetchReplyByCommentId(((Number) row[0]).intValue());
            List<ReplyResponseDTO> responses = rawResults.stream()
                    .map(r -> new ReplyResponseDTO(
                            ((Number) r[0]).intValue(),
                            ((Number) r[1]).intValue(),
                            ((Number) r[2]).intValue(),
                            (String) r[3],
                            (String) r[4],
                            (String) r[5],
                            (String) r[6],
                            ((Number) r[7]).intValue()))
                    .collect(Collectors.toList());

            comment.setReplys(responses);
            return comment;
        });
    }

    public Page<CommentResponseDTO> fetchCommentWithShareId(Integer postId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Object[]> results = commentRepository.fetchCommentWithPostId(pageable, postId);

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
            comment.setReply_count(((Number) row[9]).intValue());

            List<Object[]> rawResults = replyRepository.fetchReplyByCommentId(((Number) row[0]).intValue());
            List<ReplyResponseDTO> responses = rawResults.stream()
                    .map(r -> new ReplyResponseDTO(
                            ((Number) r[0]).intValue(),
                            ((Number) r[1]).intValue(),
                            ((Number) r[2]).intValue(),
                            (String) r[3],
                            (String) r[4],
                            (String) r[5],
                            (String) r[6],
                            ((Number) r[7]).intValue()))
                    .collect(Collectors.toList());

            comment.setReplys(responses);
            return comment;
        });
    }
}