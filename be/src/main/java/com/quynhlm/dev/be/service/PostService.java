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
import com.quynhlm.dev.be.core.exception.LocationNotFoundException;
import com.quynhlm.dev.be.core.exception.PostNotFoundException;
import com.quynhlm.dev.be.core.exception.UnknownException;
import com.quynhlm.dev.be.core.exception.UserAccountNotFoundException;
import com.quynhlm.dev.be.model.dto.requestDTO.PostRequestDTO;
import com.quynhlm.dev.be.model.dto.responseDTO.PostMediaDTO;
import com.quynhlm.dev.be.model.dto.responseDTO.PostResponseDTO;
import com.quynhlm.dev.be.model.dto.responseDTO.PostSaveResponseDTO;
import com.quynhlm.dev.be.model.dto.responseDTO.UserTagPostResponse;
import com.quynhlm.dev.be.model.dto.responseDTO.VideoPostDTO;
import com.quynhlm.dev.be.model.entity.FriendShip;
import com.quynhlm.dev.be.model.entity.HashTag;
import com.quynhlm.dev.be.model.entity.Location;
import com.quynhlm.dev.be.model.entity.Media;
import com.quynhlm.dev.be.model.entity.Post;
import com.quynhlm.dev.be.model.entity.User;
import com.quynhlm.dev.be.repositories.FriendShipRepository;
import com.quynhlm.dev.be.repositories.HashTagRespository;
import com.quynhlm.dev.be.repositories.LocationRepository;
import com.quynhlm.dev.be.repositories.MediaRepository;
import com.quynhlm.dev.be.repositories.PostRepository;
import com.quynhlm.dev.be.repositories.TagRepository;
import com.quynhlm.dev.be.repositories.UserRepository;

import lombok.extern.slf4j.Slf4j;

import org.springframework.transaction.annotation.Transactional;

import org.springframework.beans.factory.annotation.Value;

@Slf4j
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
    private LocationRepository locationRepository;

    @Autowired
    private FriendShipRepository friendShipRepository;

    @Autowired
    private HashTagRespository hashTagRespository;

    @Transactional
    public PostSaveResponseDTO insertPost(PostRequestDTO postRequestDTO, List<MultipartFile> files)
            throws UnknownException, UserAccountNotFoundException {
        try {

            User foundUser = userRepository.getAnUser(postRequestDTO.getUser_id());

            if (foundUser == null) {
                throw new UserAccountNotFoundException(
                        "Found user with " + postRequestDTO.getUser_id() + " not found . Please try again !");
            }

            Post post = new Post();
            post.setContent(postRequestDTO.getContent());
            post.setStatus(postRequestDTO.getStatus());
            post.setUser_id(postRequestDTO.getUser_id());

            Location foundLocation = locationRepository.getLocationWithLocation(postRequestDTO.getLocation());
            if(foundLocation == null){
                Location location = new Location();
                location.setAddress(postRequestDTO.getLocation());
                Location saveLocation = locationRepository.save(location);
    
                post.setLocation_id(saveLocation.getId());
            }else{
                post.setLocation_id(foundLocation.getId());
            }
        
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

                        String mediaType = (fileName != null && fileName.matches(".*\\.(jpg|jpeg|png|gif|webp)$"))
                                ? "IMAGE"
                                : "VIDEO";

                        Media media = new Media(null, savedPost.getId(),
                                String.format("https://travle-be.s3.ap-southeast-2.amazonaws.com/%s", fileName),
                                mediaType);

                        mediaRepository.save(media);
                    }
                }
            }
            if (savedPost.getId() == null) {
                throw new UnknownException("Transaction cannot complete!");
            }
            if (!postRequestDTO.getHashtags().isEmpty()) {
                for (String hashtag : postRequestDTO.getHashtags()) {
                    HashTag newHashTag = new HashTag();
                    newHashTag.setPostId(savedPost.getId());
                    newHashTag.setHashtag(hashtag);
                    hashTagRespository.save(newHashTag);
                }
            }
            return getAnPostReturnSave(savedPost.getId());
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

        Page<Object[]> results = postRepository.getAllPostsExceptFriends(friendUserIds, userId, pageable);

        return results.map(row -> {
            PostResponseDTO post = new PostResponseDTO();

            post.setOwnerId(((Number) row[0]).intValue());
            post.setPostId(((Number) row[1]).intValue());
            post.setLocationId(((Number) row[2]).intValue());
            post.setLocation((String) row[3]);
            post.setOwnerName((String) row[4]);
            post.setAvatarUrl((String) row[5]);
            post.setContent((String) row[6]);
            post.setStatus((String) row[7]);
            post.setType((String) row[8]);
            post.setIsShare(((Number) row[9]).intValue());
            post.setCreate_time((String) row[10]);
            post.setShare_by_user(row[11] != null ? ((Number) row[11]).intValue() : null);
            post.setReaction_count(((Number) row[12]).intValue());
            post.setComment_count(((Number) row[13]).intValue());
            post.setShare_count(((Number) row[14]).intValue());
            post.setIsTag(((Number) row[15]).intValue());
            post.setUser_reaction_type((String) row[16]);

            List<String> medias = mediaRepository.findMediaByPostId(((Number) row[1]).intValue());

            post.setMediaUrls(medias);

            List<String> hashtags = hashTagRespository.findHashtagByPostId(((Number) row[1]).intValue());

            post.setHashtags(hashtags);

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

    public PostMediaDTO getAnPost(Integer post_id, Integer user_id) throws PostNotFoundException {
        List<Object[]> results = postRepository.getPost(post_id, user_id);

        if (results.isEmpty()) {
            throw new PostNotFoundException(
                    "Id " + post_id + " not found or invalid data. Please try another!");
        }

        Object[] result = results.get(0);

        PostMediaDTO postMediaDTO = new PostMediaDTO();

        postMediaDTO.setOwnerId(((Number) result[0]).intValue());
        postMediaDTO.setPostId(((Number) result[1]).intValue());
        postMediaDTO.setLocationId(((Number) result[2]).intValue());
        postMediaDTO.setLocation((String) result[3]);
        postMediaDTO.setContent((String) result[4]);
        postMediaDTO.setStatus((String) result[5]);
        postMediaDTO.setFullname((String) result[6]);
        postMediaDTO.setAvatar((String) result[7]);
        postMediaDTO.setType((String) result[8]);
        postMediaDTO.setCreate_time((String) result[9]);
        postMediaDTO.setReaction_count(((Number) result[10]).intValue());
        postMediaDTO.setComment_count(((Number) result[11]).intValue());
        postMediaDTO.setShare_count(((Number) result[12]).intValue());
        postMediaDTO.setUser_reaction_type((String) result[13]);

        List<String> hashtags = hashTagRespository.findHashtagByPostId(((Number) result[1]).intValue());

        postMediaDTO.setHashtags(hashtags);

        List<String> medias = mediaRepository.findMediaByPostId(((Number) result[1]).intValue());

        postMediaDTO.setMediaUrls(medias);

        return postMediaDTO;
    }

    public PostSaveResponseDTO getAnPostReturnSave(Integer post_id) throws PostNotFoundException {
        log.info("post id : " + post_id);
        List<Object[]> results = postRepository.getPostSave(post_id);

        if (results.isEmpty()) {
            throw new PostNotFoundException(
                    "Id " + post_id + " not found or invalid data. Please try another!");
        }

        Object[] result = results.get(0);
        PostSaveResponseDTO postSaveResponseDTO = new PostSaveResponseDTO();
        postSaveResponseDTO.setOwnerId(((Number) result[0]).intValue());
        postSaveResponseDTO.setPostId(((Number) result[1]).intValue());
        postSaveResponseDTO.setLocationId(((Number) result[2]).intValue());
        postSaveResponseDTO.setLocation((String) result[3]);
        postSaveResponseDTO.setContent((String) result[4]);
        postSaveResponseDTO.setStatus((String) result[5]);
        postSaveResponseDTO.setFullname((String) result[6]);
        postSaveResponseDTO.setAvatar((String) result[7]);
        postSaveResponseDTO.setType((String) result[8]);
        postSaveResponseDTO.setCreate_time((String) result[9]);

        List<String> hashtags = hashTagRespository.findHashtagByPostId(((Number) result[1]).intValue());

        postSaveResponseDTO.setHashtags(hashtags);

        List<String> medias = mediaRepository.findMediaByPostId(((Number) result[1]).intValue());

        postSaveResponseDTO.setMediaUrls(medias);

        return postSaveResponseDTO;
    }

    // Update post

    public void updatePost(Integer post_id, PostRequestDTO postRequestDTO, List<MultipartFile> files, String newType)
            throws PostNotFoundException,LocationNotFoundException, UnknownException {
        try {
            Post foundPost = postRepository.getAnPost(post_id);
            if (foundPost == null) {
                throw new PostNotFoundException(
                        "Id " + post_id + " not found or invalid data. Please try another!");
            }
            
            Location location = locationRepository.getAnLocation(foundPost.getLocation_id());

            if(postRequestDTO.getLocation() != null){
                location.setAddress(postRequestDTO.getLocation());
                locationRepository.save(location);
            }

            if (postRequestDTO.getContent() != null) {
                foundPost.setContent(postRequestDTO.getContent());
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
            post.setLocation((String) row[3]);
            post.setContent((String) row[4]);
            post.setStatus((String) row[5]);
            post.setFullname((String) row[6]);
            post.setAvatar((String) row[7]);
            post.setVideo((String) row[8]);
            post.setCreate_time((String) row[9]);
            post.setReaction_count(((Number) row[10]).intValue());
            post.setComment_count(((Number) row[11]).intValue());
            post.setShare_count(((Number) row[12]).intValue());

            List<String> hashtags = hashTagRespository.findHashtagByPostId(((Number) row[1]).intValue());

            post.setHashtags(hashtags);
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
            post.setLocation((String) row[3]);
            post.setContent((String) row[4]);
            post.setStatus((String) row[5]);
            post.setFullname((String) row[6]);
            post.setAvatar((String) row[7]);
            post.setType((String) row[8]);
            post.setCreate_time((String) row[9]);
            post.setReaction_count(((Number) row[10]).intValue());
            post.setComment_count(((Number) row[11]).intValue());
            post.setShare_count(((Number) row[12]).intValue());

            List<String> hashtags = hashTagRespository.findHashtagByPostId(((Number) row[0]).intValue());

            post.setHashtags(hashtags);

            List<String> medias = mediaRepository.findMediaByPostId(((Number) row[0]).intValue());

            post.setMediaUrls(medias);

            return post;
        });
    }

    public Page<PostMediaDTO> foundPostByUserId(Integer user_id, Pageable pageable) {
        Page<Object[]> results = postRepository.foundPostByUserId(user_id, pageable);

        return results.map(row -> {
            PostMediaDTO post = new PostMediaDTO();
            post.setPostId(((Number) row[0]).intValue());
            post.setOwnerId(((Number) row[1]).intValue());
            post.setLocationId(((Number) row[2]).intValue());
            post.setLocation((String) row[3]);
            post.setContent((String) row[4]);
            post.setStatus((String) row[5]);
            post.setFullname((String) row[6]);
            post.setAvatar((String) row[7]);
            post.setType((String) row[8]);
            post.setCreate_time((String) row[9]);
            post.setReaction_count(((Number) row[10]).intValue());
            post.setComment_count(((Number) row[11]).intValue());
            post.setShare_count(((Number) row[12]).intValue());

            List<String> hashtags = hashTagRespository.findHashtagByPostId(((Number) row[0]).intValue());

            post.setHashtags(hashtags);

            List<String> medias = mediaRepository.findMediaByPostId(((Number) row[0]).intValue());

            post.setMediaUrls(medias);

            return post;
        });
    }
    // Feature Search
}
