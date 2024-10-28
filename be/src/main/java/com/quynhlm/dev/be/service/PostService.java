package com.quynhlm.dev.be.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;
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
import com.quynhlm.dev.be.model.entity.Media;
import com.quynhlm.dev.be.model.entity.Post;
import com.quynhlm.dev.be.repositories.MediaRepository;
import com.quynhlm.dev.be.repositories.PostRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageImpl;

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

    public Page<PostMediaDTO> getPostsWithMedia(Pageable pageable) {
        Page<Object[]> resultPage = postRepository.fetchPostWithMedia(pageable);

        List<PostMediaDTO> postMediaDTOList = resultPage.stream().map(result -> {
            int postId = (int) result[0]; // post_id
            String content = (String) result[1]; // content
            String status = (String) result[2]; // status
            int locationId = (int) result[3]; // location_id
            String hastag = (String) result[4]; // hastag
            String mediaUrl = (String) result[5]; // media_url
            String type = (String) result[6]; // type

            return new PostMediaDTO(postId, content, status, locationId, hastag, mediaUrl, type);
        }).collect(Collectors.toList());

        return new PageImpl<>(postMediaDTOList, pageable, resultPage.getTotalElements());
    }

    public void deletePost(int post_id) throws PostNotFoundException{
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
}
