package com.quynhlm.dev.be.service;

import java.io.IOException;
import java.io.InputStream;

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
import com.quynhlm.dev.be.model.entity.Comment;
import com.quynhlm.dev.be.repositories.CommentRepository;

@Service
public class CommentService {

    @Autowired
    private AmazonS3 amazonS3;

    @Autowired
    private CommentRepository commentRepository;

    @Value("${aws.s3.bucketName}")
    private String bucketName;

    public Page<Comment> getListData(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return commentRepository.findAll(pageable);
    }

    public void insertComment(Comment comment, MultipartFile imageFile) throws UnknownException {
        try {
            // Nếu có file hình ảnh, kiểm tra và tải lên S3
            if (imageFile != null && !imageFile.isEmpty()) {
                String imageFileName = imageFile.getOriginalFilename();
                long imageFileSize = imageFile.getSize();
                String imageContentType = imageFile.getContentType();

                // Kiểm tra loại file (chỉ chấp nhận ảnh)
                if (!isValidFileType(imageContentType)) {
                    throw new UnknownException("Invalid file type. Only image files are allowed.");
                }
                // Tải lên hình ảnh hoặc video lên S3
                try (InputStream mediaInputStream = imageFile.getInputStream()) {
                    ObjectMetadata mediaMetadata = new ObjectMetadata();
                    mediaMetadata.setContentLength(imageFileSize);
                    mediaMetadata.setContentType(imageContentType);

                    amazonS3.putObject(bucketName, imageFileName, mediaInputStream, mediaMetadata);

                    // Lưu đường dẫn file vào Comment (url)
                    String mediaUrl = String.format("https://travle-be.s3.ap-southeast-2.amazonaws.com/%s", imageFileName);
                    comment.setUrl(mediaUrl);
                }
            }
            commentRepository.save(comment);
        } catch (IOException e) {
            throw new UnknownException("File handling error: " + e.getMessage());
        } catch (Exception e) {
            throw new UnknownException(e.getMessage());
        }

    }

    // Hàm kiểm tra định dạng file (chỉ chấp nhận ảnh và video)
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

    public void updateComment(Integer id, String newContent, MultipartFile imageFile) throws CommentNotFoundException, UnknownException {
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

}