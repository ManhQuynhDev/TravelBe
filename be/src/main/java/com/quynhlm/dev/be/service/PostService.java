package com.quynhlm.dev.be.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.sql.Timestamp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.quynhlm.dev.be.core.exception.PostNotFoundException;
import com.quynhlm.dev.be.core.exception.UnknownException;
import com.quynhlm.dev.be.model.dto.responseDTO.PostMediaDTO;
import com.quynhlm.dev.be.model.dto.responseDTO.PostResponseDTO;
import com.quynhlm.dev.be.model.dto.responseDTO.VideoPostDTO;
import com.quynhlm.dev.be.model.entity.Media;
import com.quynhlm.dev.be.model.entity.Post;
import com.quynhlm.dev.be.repositories.MediaRepository;
import com.quynhlm.dev.be.repositories.PostRepository;
import org.springframework.beans.factory.annotation.Value;

@Service
public class PostService {

    @Autowired
    private AmazonS3 amazonS3;

    @Value("${aws.s3.bucketName}")
    private String bucketName;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private MediaRepository mediaRepository;

    public void insertPost(Post post, List<MultipartFile> files, String type) throws UnknownException {
        try {
            post.setCreate_time(new Timestamp(System.currentTimeMillis()).toString());
            Post savedPost = postRepository.save(post);

            if (files != null && !files.isEmpty()) {
                for (MultipartFile file : files) {
                    if (file.isEmpty()) {
                        continue;
                    }

                    String fileName = file.getOriginalFilename();
                    long fileSize = file.getSize();
                    String contentType = file.getContentType();

                    try (InputStream inputStream = file.getInputStream()) {

                        ObjectMetadata metadata = new ObjectMetadata();
                        metadata.setContentLength(fileSize);
                        metadata.setContentType(contentType);

                        amazonS3.putObject(bucketName, fileName, inputStream, metadata);
                        if (savedPost.getId() == null) {
                            throw new UnknownException("Transaction cannot complete!");
                        }

                        Media media = new Media(null, savedPost.getId(),
                                String.format("https://travle-be.s3.ap-southeast-2.amazonaws.com/%s", fileName),
                                type);

                        mediaRepository.save(media);
                    }
                }
            }
        } catch (IOException e) {
            throw new UnknownException("File handling error: " + e.getMessage());
        } catch (Exception e) {
            throw new UnknownException(e.getMessage());
        }
    }

    public Page<PostResponseDTO> getAllPostsAndSharedPosts(Pageable pageable) {
        Page<Object[]> results = postRepository.getAllPostsAndSharedPosts(pageable);

        return results.map(row -> {
            PostResponseDTO post = new PostResponseDTO();
            post.setOwnerId(((Number) row[0]).intValue());
            post.setPostId(((Number) row[1]).intValue());
            post.setContent((String) row[2]);
            post.setMediaUrl((String) row[3]);
            post.setLocationId(((Number) row[4]).intValue());
            post.setHastag((String) row[5]);
            post.setStatus((String) row[6]);
            post.setType((String) row[7]);
            post.setIsShare(((Number) row[8]).intValue());
            post.setCreate_time((String) row[9]);
            post.setShareByUser(row[10] != null ? ((Number) row[10]).intValue() : null);
            post.setReaction_count(((Number) row[11]).intValue());
            post.setComment_count(((Number) row[12]).intValue());
            post.setShare_count(((Number) row[13]).intValue());
            return post;
        });
    }

    public void deletePost(int post_id) throws PostNotFoundException {
        Post foundPost = postRepository.getAnPost(post_id);

        if (foundPost == null) {
            throw new PostNotFoundException("Id " + post_id + " not found. Please try another!");
        }

        Media media = mediaRepository.foundMediaByPostId(post_id);
        if (media != null) {
            mediaRepository.delete(media);
        }

        postRepository.delete(foundPost);
    }

    public PostMediaDTO getAnPost(Integer post_id) throws PostNotFoundException {
        List<Object[]> results = postRepository.getPost(post_id);

        if (results.isEmpty()) {
            throw new PostNotFoundException(
                    "Id " + post_id + " not found or invalid data. Please try another!");
        }

        Object[] result = results.get(0);

        int ownerId = (int) result[0];
        int postId = (int) result[1];
        String content = (String) result[2];
        String mediaUrl = (String) result[3];
        int locationId = (int) result[4];
        String status = (String) result[5];
        String type = (String) result[6];
        String create_time = (String) result[7];

        return new PostMediaDTO(ownerId, postId, content, mediaUrl, locationId, status, type, create_time);
    }

    // Update post

    public void updatePost(Integer post_id, Post newPost, List<MultipartFile> files, String newType)
            throws PostNotFoundException, UnknownException {
        try {
            // Tìm bài viết đã tồn tại
            Post foundPost = postRepository.getAnPost(post_id);
            if (foundPost == null) {
                throw new PostNotFoundException(
                        "Id " + post_id + " not found or invalid data. Please try another!");
            }

            if (newPost.getContent() != null) {
                foundPost.setContent(newPost.getContent());
            }
            if (newPost.getLocation_id() != null) {
                foundPost.setLocation_id(newPost.getLocation_id());
            }
            if (newPost.getStatus() != null) {
                foundPost.setStatus(newPost.getStatus());
            }
            if (newPost.getHastag() != null) {
                foundPost.setHastag(newPost.getHastag());
            }

            // Nếu người dùng có tải lên các tệp tin mới
            if (files != null && !files.isEmpty()) {
                for (MultipartFile file : files) {
                    if (file.isEmpty()) {
                        continue; // Bỏ qua tệp rỗng
                    }

                    String fileName = file.getOriginalFilename();
                    long fileSize = file.getSize();
                    String contentType = file.getContentType();

                    try (InputStream inputStream = file.getInputStream()) {
                        ObjectMetadata metadata = new ObjectMetadata();
                        metadata.setContentLength(fileSize);
                        metadata.setContentType(contentType);

                        // Tải tệp lên S3
                        amazonS3.putObject(bucketName, fileName, inputStream, metadata);

                        // Cập nhật thông tin media
                        Media media = mediaRepository.foundMediaByPostId(post_id);
                        if (media != null) {
                            String mediaUrl = String.format("https://travle-be.s3.ap-southeast-2.amazonaws.com/%s",
                                    fileName);
                            media.setMedia_url(mediaUrl);
                            media.setType(newType);
                            mediaRepository.save(media);
                        } else {
                            // Nếu không tìm thấy media, có thể thêm logic để tạo mới nếu cần
                        }
                    }
                }
            }

            // Lưu bài viết đã cập nhật
            Post savedPost = postRepository.save(foundPost);
            if (savedPost.getId() == null) {
                throw new UnknownException("Transaction cannot be completed!");
            }
        } catch (IOException e) {
            throw new UnknownException("File handling error: " + e.getMessage());
        } catch (Exception e) {
            throw new UnknownException(e.getMessage());
        }
    }

    // Get All Video
    public Page<VideoPostDTO> getAllPostTypeVideo(Pageable pageable) {
        Page<Object[]> results = postRepository.fetchPostWithMediaTypeVideo(pageable);

        return results.map(row -> {
            VideoPostDTO post = new VideoPostDTO();
            post.setOwnerId(((Number) row[0]).intValue());
            post.setPostId(((Number) row[1]).intValue());
            post.setLocationId(((Number) row[2]).intValue());
            post.setContent((String) row[3]);
            post.setStatus((String) row[4]);
            post.setFullname((String) row[5]);
            post.setAvatar((String) row[6]);
            post.setVideo((String) row[7]);
            post.setCreate_time((String) row[8]);
            post.setReaction_count(((Number) row[9]).intValue());
            post.setComment_count(((Number) row[10]).intValue());
            post.setShare_count(((Number) row[11]).intValue());
            return post;
        });
    }

    // Feature Search
}
