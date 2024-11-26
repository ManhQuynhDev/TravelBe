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
import com.quynhlm.dev.be.core.exception.UserAccountNotFoundException;
import com.quynhlm.dev.be.model.dto.requestDTO.PostRequestDTO;
import com.quynhlm.dev.be.model.dto.responseDTO.PostMediaDTO;
import com.quynhlm.dev.be.model.dto.responseDTO.PostResponseDTO;
import com.quynhlm.dev.be.model.dto.responseDTO.UserTagPostResponse;
import com.quynhlm.dev.be.model.dto.responseDTO.VideoPostDTO;
import com.quynhlm.dev.be.model.entity.FriendShip;
import com.quynhlm.dev.be.model.entity.Media;
import com.quynhlm.dev.be.model.entity.Post;
import com.quynhlm.dev.be.model.entity.User;
import com.quynhlm.dev.be.repositories.FriendShipRepository;
import com.quynhlm.dev.be.repositories.MediaRepository;
import com.quynhlm.dev.be.repositories.PostRepository;
import com.quynhlm.dev.be.repositories.TagRepository;
import com.quynhlm.dev.be.repositories.UserRepository;

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

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FriendShipRepository friendShipRepository;

    public Post insertPost(PostRequestDTO postRequestDTO, List<MultipartFile> files, String type)
            throws UnknownException {
        try {
            Post post = new Post();
            post.setContent(postRequestDTO.getContent());
            post.setStatus(postRequestDTO.getStatus());
            post.setUser_id(postRequestDTO.getUser_id());
            post.setLocation_id(postRequestDTO.getLocation_id());

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
            return post;
        } catch (IOException e) {
            throw new UnknownException("File handling error: " + e.getMessage());
        } catch (Exception e) {
            throw new UnknownException(e.getMessage());
        }
    }

    // Get all list friend post
    public Page<PostResponseDTO> getAllPostsAndSharedPosts(Integer userId, Pageable pageable)
            throws UserAccountNotFoundException {

        User foundUser = userRepository.getAnUser(userId);

        if (foundUser == null) {
            throw new UserAccountNotFoundException("Found user with " + userId + " not found . Please try again !");
        }

        List<FriendShip> friendShips = friendShipRepository.fetchByUserReceivedIdAndStatus(userId, "APPROVED");

        List<Integer> friendUserIds = friendShips.stream()
                .map(FriendShip::getUserSendId)
                .collect(Collectors.toList());

        System.out.println("UserId :" + userId);
        System.out.println("Friends 2" + friendShips.size());
        System.out.println("Friends" + friendUserIds.size());

        Page<Object[]> results = postRepository.getAllPostsExceptFriends(friendUserIds, userId, pageable);

        return results.map(row -> {
            PostResponseDTO post = new PostResponseDTO();

            post.setOwnerId(((Number) row[0]).intValue());
            post.setPostId(((Number) row[1]).intValue());
            post.setLocationId(((Number) row[2]).intValue());
            post.setAdminName((String) row[3]);
            post.setAvatarUrl((String) row[4]);
            post.setContent((String) row[5]);
            post.setHastag((String) row[6]);
            post.setStatus((String) row[7]);
            post.setType((String) row[8]);
            post.setIsShare(((Number) row[9]).intValue());
            post.setCreate_time((String) row[10]);
            post.setShare_by_user(row[11] != null ? ((Number) row[11]).intValue() : null);
            post.setReaction_count(((Number) row[12]).intValue());
            post.setComment_count(((Number) row[13]).intValue());
            post.setShare_count(((Number) row[14]).intValue());
            post.setIsTag(((Number) row[15]).intValue());

            List<String> medias = mediaRepository.findMediaByPostId(((Number) row[1]).intValue());

            post.setMediaUrls(medias);

            if (((Number) row[15]).intValue() >= 1) {
                List<Object[]> rawResults = tagRepository.foundUserTagPost(((Number) row[1]).intValue());
                List<UserTagPostResponse> responses = rawResults.stream()
                        .map(u -> new UserTagPostResponse(
                                ((Number) u[0]).intValue(),
                                (String) u[1],
                                (String) u[2]))
                        .collect(Collectors.toList());

                post.setTags(responses);
            }
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

        Integer ownerId = ((Number) result[0]).intValue();
        Integer postId = ((Number) result[1]).intValue();
        Integer locationId = ((Number) result[2]).intValue();
        String content = (String) result[3];
        String status = (String) result[4];
        String fullname = (String) result[5];
        String avatar = (String) result[6];
        String mediaUrl = (String) result[7];
        String type = (String) result[8];
        String create_time = (String) result[9];
        Integer reaction_count = ((Number) result[10]).intValue();
        Integer comment_count = ((Number) result[11]).intValue();
        Integer share_count = ((Number) result[12]).intValue();

        return new PostMediaDTO(ownerId, postId, locationId,content,status,fullname,avatar , mediaUrl , type , create_time , reaction_count , comment_count , share_count);
    }

    // Update post

    public void updatePost(Integer post_id, PostRequestDTO postRequestDTO, List<MultipartFile> files, String newType)
            throws PostNotFoundException, UnknownException {
        try {

            Post foundPost = postRepository.getAnPost(post_id);
            if (foundPost == null) {
                throw new PostNotFoundException(
                        "Id " + post_id + " not found or invalid data. Please try another!");
            }

            if (postRequestDTO.getContent() != null) {
                foundPost.setContent(postRequestDTO.getContent());
            }
            if (postRequestDTO.getLocation_id() != null) {
                foundPost.setLocation_id(postRequestDTO.getLocation_id());
            }
            if (postRequestDTO.getStatus() != null) {
                foundPost.setStatus(postRequestDTO.getStatus());
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
            
            post.setPostId(((Number) row[0]).intValue());
            post.setOwnerId(((Number) row[1]).intValue());
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

    // Get All Post
    public Page<PostMediaDTO> getAllPost(Pageable pageable) {
        Page<Object[]> results = postRepository.fetchAllPost(pageable);

        return results.map(row -> {
            PostMediaDTO post = new PostMediaDTO();
            
            post.setPostId(((Number) row[0]).intValue());
            post.setOwnerId(((Number) row[1]).intValue());
            post.setLocationId(((Number) row[2]).intValue());
            post.setContent((String) row[3]);
            post.setStatus((String) row[4]);
            post.setFullname((String) row[5]);
            post.setAvatar((String) row[6]);
            post.setMediaUrl((String) row[7]);
            post.setType((String) row[8]);
            post.setCreate_time((String) row[9]);
            post.setReaction_count(((Number) row[10]).intValue());
            post.setComment_count(((Number) row[11]).intValue());
            post.setShare_count(((Number) row[12]).intValue());
            return post;
        });
    }
    // Feature Search
}
