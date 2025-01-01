package com.quynhlm.dev.be.service;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

import java.sql.Timestamp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.quynhlm.dev.be.core.exception.LocationNotFoundException;
import com.quynhlm.dev.be.core.exception.PostNotFoundException;
import com.quynhlm.dev.be.core.exception.UnknownException;
import com.quynhlm.dev.be.core.exception.UserAccountNotFoundException;
import com.quynhlm.dev.be.model.dto.requestDTO.PostRequestDTO;
import com.quynhlm.dev.be.model.dto.requestDTO.ShareRequestDTO;
import com.quynhlm.dev.be.model.dto.responseDTO.MediaResponseDTO;
import com.quynhlm.dev.be.model.dto.responseDTO.PostMediaDTO;
import com.quynhlm.dev.be.model.dto.responseDTO.PostResponseDTO;
import com.quynhlm.dev.be.model.dto.responseDTO.PostStatisticalDTO;
import com.quynhlm.dev.be.model.dto.responseDTO.ReactionCountDTO;
import com.quynhlm.dev.be.model.entity.HashTag;
import com.quynhlm.dev.be.model.entity.Location;
import com.quynhlm.dev.be.model.entity.Media;
import com.quynhlm.dev.be.model.entity.Post;
import com.quynhlm.dev.be.model.entity.User;
import com.quynhlm.dev.be.repositories.CommentRepository;
import com.quynhlm.dev.be.repositories.HashTagRespository;
import com.quynhlm.dev.be.repositories.LocationRepository;
import com.quynhlm.dev.be.repositories.MediaRepository;
import com.quynhlm.dev.be.repositories.PostReactionRepository;
import com.quynhlm.dev.be.repositories.PostRepository;
import com.quynhlm.dev.be.repositories.ReviewRepository;
import com.quynhlm.dev.be.repositories.UserRepository;

import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;

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
    private ReviewRepository reviewRepository;

    @Autowired
    private MediaRepository mediaRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private PostReactionRepository postReactionRepository;

    @Autowired
    private HashTagRespository hashTagRespository;

    @Autowired
    private CommentRepository commentRepository;

    // INSERT POST
    @Transactional
    public PostResponseDTO insertPost(PostRequestDTO postRequestDTO, List<MultipartFile> files)
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
            if (foundLocation == null) {
                Location location = new Location();
                location.setAddress(postRequestDTO.getLocation());
                Location saveLocation = locationRepository.save(location);

                post.setLocation_id(saveLocation.getId());
            } else {
                post.setLocation_id(foundLocation.getId());
            }

            post.setCreate_time(new Timestamp(System.currentTimeMillis()).toString());
            post.setIsShare(0);
            post.setDelflag(0);
            Post savedPost = postRepository.save(post);
            if (files != null && !files.isEmpty()) {
                for (MultipartFile file : files) {
                    if (file.isEmpty()) {
                        continue;
                    }

                    String mediaUrl = uploadMediaToS3(file);
                    String mediaType = (mediaUrl != null && mediaUrl.matches(".*\\.(jpg|jpeg|png|gif|webp)$"))
                            ? "IMAGE"
                            : "VIDEO";

                    Media media = new Media(null, savedPost.getId(),
                            mediaUrl,
                            mediaType);

                    mediaRepository.save(media);
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

    private String uploadMediaToS3(MultipartFile file) throws IOException, UnknownException {
        String fileName = file.getOriginalFilename();
        String contentType = file.getContentType();

        try (InputStream inputStream = file.getInputStream()) {
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
            String mediaUrl = String.format("https://travle-be.s3.ap-southeast-2.amazonaws.com/%s",
                    fileName);
            return mediaUrl;
        } catch (Exception e) {
            throw new UnknownException("Error uploading file to S3: " + e.getMessage());
        }
    }

    // SHARE POST
    @Transactional
    public PostResponseDTO sharePost(ShareRequestDTO shareRequestDTO)
            throws PostNotFoundException, UnknownException, UserAccountNotFoundException {

        Post foundPost = postRepository.getAnPost(shareRequestDTO.getPostId());
        if (foundPost == null) {
            throw new PostNotFoundException(
                    "Id " + shareRequestDTO.getPostId() + " not found or invalid data. Please try another!");
        }

        User foundUser = userRepository.getAnUser(shareRequestDTO.getShareById());
        if (foundUser == null) {
            throw new UserAccountNotFoundException(
                    "User with ID " + shareRequestDTO.getShareById() + " not found. Please try again!");
        }

        Post sharePost = buildSharedPost(foundPost, foundUser, shareRequestDTO);
        Post savedPost = postRepository.save(sharePost);

        if (savedPost.getId() == null) {
            throw new UnknownException("Transaction cannot be completed!");
        }

        saveHashTags(shareRequestDTO.getHashtags(), savedPost.getId());

        return getAnPostReturnSave(savedPost.getId());
    }

    private Post buildSharedPost(Post foundPost, User foundUser, ShareRequestDTO shareRequestDTO) {
        log.info(shareRequestDTO.getShareById() + "");
        Post sharePost = new Post();
        sharePost.setDelflag(0);
        sharePost.setContent(foundPost.getContent());
        sharePost.setUser_id(foundPost.getUser_id());
        sharePost.setLocation_id(foundPost.getLocation_id());
        sharePost.setStatus(foundPost.getStatus());
        sharePost.setPost_id(foundPost.getIsShare() == 0 ? shareRequestDTO.getPostId() : foundPost.getPost_id());
        sharePost.setIsShare(1);
        sharePost.setShareContent(shareRequestDTO.getContent());
        sharePost.setShare_by_id(shareRequestDTO.getShareById());
        sharePost.setStatusShare(shareRequestDTO.getStatus());
        sharePost.setCreate_time(new Timestamp(System.currentTimeMillis()).toString());
        sharePost.setShare_time(new Timestamp(System.currentTimeMillis()).toString());
        return sharePost;
    }

    private void saveHashTags(List<String> hashtags, Integer postId) {
        if (hashtags != null && !hashtags.isEmpty()) {
            for (String hashtag : hashtags) {
                HashTag newHashTag = new HashTag();
                newHashTag.setPostId(postId);
                newHashTag.setHashtag(hashtag);
                hashTagRespository.save(newHashTag);
            }
        }
    }

    // GET ALL
    public Page<PostResponseDTO> getAllPostsAndSharedPosts(Integer userId, Pageable pageable)
            throws UserAccountNotFoundException {

        User foundUser = userRepository.getAnUser(userId);

        if (foundUser == null) {
            throw new UserAccountNotFoundException("Found user with " + userId + " not found . Please try again !");
        }

        Page<Object[]> results = postRepository.getAllPostsAndSharedPosts(userId, pageable);

        return results.map(row -> {
            PostResponseDTO post = new PostResponseDTO();
            post.setOwnerId(((Number) row[0]).intValue());
            post.setPostId(((Number) row[1]).intValue());
            post.setLocationId(((Number) row[2]).intValue());
            post.setLocation((String) row[3]);
            post.setOwnerName((String) row[4]);
            post.setAvatarUrl((String) row[5]);
            post.setPostContent(row[6] != null ? ((String) row[6]) : null);
            post.setStatus((String) row[7]);
            post.setCreate_time((String) row[8]);
            post.setIsShare(((Number) row[9]).intValue());
            post.setUser_share_id(row[10] != null ? ((Number) row[10]).intValue() : null);
            post.setUser_share_name(row[11] != null ? ((String) row[11]) : null);
            post.setUser_share_avatar(row[12] != null ? ((String) row[12]) : null);
            post.setShareContent(row[13] != null ? ((String) row[13]) : null);
            post.setShare_time(row[14] != null ? ((String) row[14]) : null);
            post.setShare_status(row[15] != null ? ((String) row[15]) : null);
            post.setReaction_count(((Number) row[16]).intValue());
            // post.setComment_count(((Number) row[17]).intValue());
            post.setShare_count(((Number) row[18]).intValue());
            post.setUser_reaction_type((String) row[19]);
            post.setShare_post_id(row[20] != null ? ((Number) row[20]).intValue() : null);

            Integer comment_count = commentRepository.commentCountWithPostId(((Number) row[1]).intValue());
            post.setComment_count(comment_count == null ? 0 : comment_count);

            Double averageRating = reviewRepository.averageStarWithLocation(((Number) row[2]).intValue());
            post.setAverageRating(averageRating != null ? averageRating : 0.0);
            List<String> medias;
            if (((Number) row[9]).intValue() == 0) {
                medias = mediaRepository.findMediaByPostId(((Number) row[1]).intValue());
            } else {
                medias = mediaRepository.findMediaByPostId(((Number) row[20]).intValue());
            }

            List<MediaResponseDTO> mediaResponseDTOs = medias.stream().map(mediaUrl -> {
                MediaResponseDTO mediaResponseDTO = new MediaResponseDTO();
                mediaResponseDTO.setMediaUrl(mediaUrl);
                mediaResponseDTO.setMediaType(mediaUrl.matches(".*\\.(jpg|jpeg|png|gif|webp)$") ? "IMAGE" : "VIDEO");
                return mediaResponseDTO;
            }).collect(Collectors.toList());

            post.setMediaUrls(mediaResponseDTOs);

            List<String> hashtags = hashTagRespository.findHashtagByPostId(((Number) row[1]).intValue());

            post.setHashtags(hashtags);

            List<Object[]> reactions = postReactionRepository.findTopReactions(((Number) row[1]).intValue());

            if (!reactions.isEmpty()) {
                List<ReactionCountDTO> reactionStatisticsList = new ArrayList<>();

                for (Object[] result : reactions) {
                    ReactionCountDTO reactionStatisticsDTO = new ReactionCountDTO();
                    reactionStatisticsDTO.setType((String) result[0]);
                    reactionStatisticsDTO.setReactionCount(((Number) result[1]).intValue());
                    reactionStatisticsList.add(reactionStatisticsDTO);
                }
                post.setReactionStatistics(reactionStatisticsList);
            } else {
                List<ReactionCountDTO> newLis = new ArrayList<>();
                post.setReactionStatistics(newLis);
            }
            return post;
        });
    }

    // GET AN POST
    public PostResponseDTO getAnPostWithPostId(Integer postId, Integer userId)
            throws UserAccountNotFoundException, PostNotFoundException {

        Post foundPost = postRepository.getAnPost(postId);
        if (foundPost == null) {
            throw new PostNotFoundException("Id " + postId + " not found. Please try another!");
        }

        User foundUser = userRepository.getAnUser(userId);

        if (foundUser == null) {
            throw new UserAccountNotFoundException("Found user with " + userId + " not found . Please try again !");
        }

        List<Object[]> results = postRepository.getPostWithPostId(postId, userId);

        Object[] row = results.get(0);

        PostResponseDTO post = new PostResponseDTO();
        post.setOwnerId(((Number) row[0]).intValue());
        post.setPostId(((Number) row[1]).intValue());
        post.setLocationId(((Number) row[2]).intValue());
        post.setLocation((String) row[3]);
        post.setOwnerName((String) row[4]);
        post.setAvatarUrl((String) row[5]);
        post.setPostContent(row[6] != null ? ((String) row[6]) : null);
        post.setStatus((String) row[7]);
        post.setCreate_time((String) row[8]);
        post.setIsShare(((Number) row[9]).intValue());
        post.setUser_share_id(row[10] != null ? ((Number) row[10]).intValue() : null);
        post.setUser_share_name(row[11] != null ? ((String) row[11]) : null);
        post.setUser_share_avatar(row[12] != null ? ((String) row[12]) : null);
        post.setShareContent(row[13] != null ? ((String) row[13]) : null);
        post.setShare_time(row[14] != null ? ((String) row[14]) : null);
        post.setShare_status(row[15] != null ? ((String) row[15]) : null);
        post.setReaction_count(((Number) row[16]).intValue());
        // post.setComment_count(((Number) row[17]).intValue());
        post.setShare_count(((Number) row[18]).intValue());
        post.setUser_reaction_type((String) row[19]);
        post.setShare_post_id(row[20] != null ? ((Number) row[20]).intValue() : null);

        Integer comment_count = commentRepository.commentCountWithPostId(((Number) row[1]).intValue());
        post.setComment_count(comment_count == null ? 0 : comment_count);

        Double averageRating = reviewRepository.averageStarWithLocation(((Number) row[2]).intValue());
        post.setAverageRating(averageRating != null ? averageRating : 0.0);

        List<String> medias;

        if (((Number) row[9]).intValue() == 0) {
            medias = mediaRepository.findMediaByPostId(((Number) row[1]).intValue());
        } else {
            medias = mediaRepository.findMediaByPostId(((Number) row[20]).intValue());
        }

        List<MediaResponseDTO> mediaResponseDTOs = medias.stream().map(mediaUrl -> {
            MediaResponseDTO mediaResponseDTO = new MediaResponseDTO();
            mediaResponseDTO.setMediaUrl(mediaUrl);
            mediaResponseDTO.setMediaType(mediaUrl.matches(".*\\.(jpg|jpeg|png|gif|webp)$") ? "IMAGE" : "VIDEO");
            return mediaResponseDTO;
        }).collect(Collectors.toList());

        post.setMediaUrls(mediaResponseDTOs);

        List<String> hashtags = hashTagRespository.findHashtagByPostId(((Number) row[1]).intValue());

        List<Object[]> reactions = postReactionRepository.findTopReactions(((Number) row[1]).intValue());

        if (!reactions.isEmpty()) {
            List<ReactionCountDTO> reactionStatisticsList = new ArrayList<>();

            for (Object[] result : reactions) {
                ReactionCountDTO reactionStatisticsDTO = new ReactionCountDTO();
                reactionStatisticsDTO.setType((String) result[0]);
                reactionStatisticsDTO.setReactionCount(((Number) result[1]).intValue());
                reactionStatisticsList.add(reactionStatisticsDTO);
            }
            post.setReactionStatistics(reactionStatisticsList);
        } else {
            List<ReactionCountDTO> newLis = new ArrayList<>();
            post.setReactionStatistics(newLis);
        }

        post.setHashtags(hashtags);
        return post;
    }

    // DELETE POST
    public void deletePost(int post_id) throws PostNotFoundException {
        Post foundPost = postRepository.getAnPost(post_id);

        if (foundPost == null) {
            throw new PostNotFoundException("Id " + post_id + " not found. Please try another!");
        }

        List<Media> medias = mediaRepository.foundMediaByPostId(post_id);
        for (Media media : medias) {
            mediaRepository.delete(media);
        }

        foundPost.setDelflag(1);
        postRepository.save(foundPost);
    }

    public Page<PostMediaDTO> searchPostWithHashtag(String keyword, Integer user_id, int page, int size)
            throws PostNotFoundException {

        Pageable pageable = PageRequest.of(page, size);
        Page<Object[]> results = postRepository.searchByHashTag(keyword, user_id, pageable);

        return results.map(result -> {
            PostMediaDTO postMediaDTO = new PostMediaDTO();

            postMediaDTO.setOwnerId(((Number) result[0]).intValue());
            postMediaDTO.setPostId(((Number) result[1]).intValue());
            postMediaDTO.setLocationId(((Number) result[2]).intValue());
            postMediaDTO.setLocation((String) result[3]);
            postMediaDTO.setContent((String) result[4]);
            postMediaDTO.setStatus((String) result[5]);
            postMediaDTO.setFullname((String) result[6]);
            postMediaDTO.setAvatar((String) result[7]);
            postMediaDTO.setCreate_time((String) result[8]);
            postMediaDTO.setReaction_count(((Number) result[9]).intValue());
            // postMediaDTO.setComment_count(((Number) result[10]).intValue());
            postMediaDTO.setShare_count(((Number) result[11]).intValue());
            postMediaDTO.setUser_reaction_type((String) result[12]);

            Integer comment_count = commentRepository.commentCountWithPostId(((Number) result[1]).intValue());
            postMediaDTO.setComment_count(comment_count == null ? 0 : comment_count);

            List<String> hashtags = hashTagRespository.findHashtagByPostId(((Number) result[1]).intValue());

            postMediaDTO.setHashtags(hashtags);

            List<String> medias = mediaRepository.findMediaByPostId(((Number) result[1]).intValue());

            postMediaDTO.setMediaUrls(medias);

            List<Object[]> reactions = postReactionRepository.findTopReactions(((Number) result[1]).intValue());

            if (!reactions.isEmpty()) {
                List<ReactionCountDTO> reactionStatisticsList = new ArrayList<>();

                for (Object[] reaction : reactions) {
                    ReactionCountDTO reactionStatisticsDTO = new ReactionCountDTO();
                    reactionStatisticsDTO.setType((String) reaction[0]);
                    reactionStatisticsDTO.setReactionCount(((Number) reaction[1]).intValue());
                    reactionStatisticsList.add(reactionStatisticsDTO);
                }
                postMediaDTO.setReactionStatistics(reactionStatisticsList);
            } else {
                List<ReactionCountDTO> newLis = new ArrayList<>();
                postMediaDTO.setReactionStatistics(newLis);
            }

            return postMediaDTO;
        });
    }

    public Page<PostMediaDTO> searchPostWithContent(Integer user_id, String keyword, int page, int size)
            throws PostNotFoundException {
        Pageable pageable = PageRequest.of(page, size);
        Page<Object[]> results = postRepository.searchPostWithContent(keyword, user_id, pageable);

        return results.map(row -> {
            PostMediaDTO postMediaDTO = new PostMediaDTO();

            postMediaDTO.setOwnerId(((Number) row[0]).intValue());
            postMediaDTO.setPostId(((Number) row[1]).intValue());
            postMediaDTO.setLocationId(((Number) row[2]).intValue());
            postMediaDTO.setLocation((String) row[3]);
            postMediaDTO.setContent((String) row[4]);
            postMediaDTO.setStatus((String) row[5]);
            postMediaDTO.setFullname((String) row[6]);
            postMediaDTO.setAvatar((String) row[7]);
            postMediaDTO.setType((String) row[8]);
            postMediaDTO.setCreate_time((String) row[9]);
            postMediaDTO.setReaction_count(((Number) row[10]).intValue());
            // postMediaDTO.setComment_count(((Number) row[11]).intValue());
            postMediaDTO.setShare_count(((Number) row[12]).intValue());
            postMediaDTO.setUser_reaction_type((String) row[13]);

            Integer comment_count = commentRepository.commentCountWithPostId(((Number) row[1]).intValue());
            postMediaDTO.setComment_count(comment_count == null ? 0 : comment_count);

            List<String> hashtags = hashTagRespository.findHashtagByPostId(((Number) row[1]).intValue());

            postMediaDTO.setHashtags(hashtags);

            List<String> medias = mediaRepository.findMediaByPostId(((Number) row[1]).intValue());

            postMediaDTO.setMediaUrls(medias);

            List<Object[]> reactions = postReactionRepository.findTopReactions(((Number) row[1]).intValue());

            if (!reactions.isEmpty()) {
                List<ReactionCountDTO> reactionStatisticsList = new ArrayList<>();

                for (Object[] result : reactions) {
                    ReactionCountDTO reactionStatisticsDTO = new ReactionCountDTO();
                    reactionStatisticsDTO.setType((String) result[0]);
                    reactionStatisticsDTO.setReactionCount(((Number) result[1]).intValue());
                    reactionStatisticsList.add(reactionStatisticsDTO);
                }
                postMediaDTO.setReactionStatistics(reactionStatisticsList);
            } else {
                List<ReactionCountDTO> newLis = new ArrayList<>();
                postMediaDTO.setReactionStatistics(newLis);
            }
            return postMediaDTO;
        });
    }

    // GET AN POST RETURN SAVE
    public PostResponseDTO getAnPostReturnSave(Integer post_id) throws PostNotFoundException {
        Post foundPost = postRepository.getAnPost(post_id);
        if (foundPost == null) {
            throw new PostNotFoundException("Id " + post_id + " not found. Please try another!");
        }

        List<Object[]> results = postRepository.getPostSave(post_id);

        Object[] row = results.get(0);

        PostResponseDTO post = new PostResponseDTO();
        post.setOwnerId(((Number) row[0]).intValue());
        post.setPostId(((Number) row[1]).intValue());
        post.setLocationId(((Number) row[2]).intValue());
        post.setLocation((String) row[3]);
        post.setOwnerName((String) row[4]);
        post.setAvatarUrl((String) row[5]);
        post.setPostContent(row[6] != null ? ((String) row[6]) : null);
        post.setStatus((String) row[7]);
        post.setCreate_time((String) row[8]);
        post.setIsShare(((Number) row[9]).intValue());
        post.setUser_share_id(row[10] != null ? ((Number) row[10]).intValue() : null);
        post.setUser_share_name(row[11] != null ? ((String) row[11]) : null);
        post.setUser_share_avatar(row[12] != null ? ((String) row[12]) : null);
        post.setShareContent(row[13] != null ? ((String) row[13]) : null);
        post.setShare_time(row[14] != null ? ((String) row[14]) : null);
        post.setShare_status(row[15] != null ? ((String) row[15]) : null);
        post.setReaction_count(((Number) row[16]).intValue());
        post.setComment_count(((Number) row[17]).intValue());
        post.setShare_count(((Number) row[18]).intValue());
        post.setShare_post_id(row[19] != null ? ((Number) row[19]).intValue() : null);

        Double averageRating = reviewRepository.averageStarWithLocation(((Number) row[2]).intValue());
        post.setAverageRating(averageRating != null ? averageRating : 0.0);

        List<String> medias;
        if (((Number) row[9]).intValue() == 0) {
            medias = mediaRepository.findMediaByPostId(((Number) row[1]).intValue());
        } else {
            medias = mediaRepository.findMediaByPostId(((Number) row[19]).intValue());
        }

        List<MediaResponseDTO> mediaResponseDTOs = medias.stream().map(mediaUrl -> {
            MediaResponseDTO mediaResponseDTO = new MediaResponseDTO();
            mediaResponseDTO.setMediaUrl(mediaUrl);
            mediaResponseDTO.setMediaType(mediaUrl.matches(".*\\.(jpg|jpeg|png|gif|webp)$") ? "IMAGE" : "VIDEO");
            return mediaResponseDTO;
        }).collect(Collectors.toList());

        post.setMediaUrls(mediaResponseDTOs);

        List<String> hashtags = hashTagRespository.findHashtagByPostId(((Number) row[1]).intValue());

        post.setHashtags(hashtags);
        return post;
    }

    public void updatePost(Integer post_id, PostRequestDTO postRequestDTO, List<MultipartFile> files)
            throws PostNotFoundException, LocationNotFoundException, UnknownException {
        try {
            // Tìm bài viết
            Post foundPost = postRepository.getAnPost(post_id);
            if (foundPost == null) {
                throw new PostNotFoundException("Id " + post_id + " not found or invalid data. Please try another!");
            }

            Location location = locationRepository.getAnLocation(foundPost.getLocation_id());
            if (postRequestDTO.getLocation() != null) {
                location.setAddress(postRequestDTO.getLocation());
                locationRepository.save(location);
            }

            if (postRequestDTO.getContent() != null) {
                foundPost.setContent(postRequestDTO.getContent());
            }
            if (postRequestDTO.getStatus() != null) {
                foundPost.setStatus(postRequestDTO.getStatus());
            }

            List<Media> currentMedias = mediaRepository.foundMediaByPostId(post_id);
            Set<String> newMediaUrls = new HashSet<>();

            if (files != null && !files.isEmpty()) {
                for (MultipartFile file : files) {
                    if (file.isEmpty()) {
                        continue;
                    }

                    String fileName = file.getOriginalFilename();
                    long fileSize = file.getSize();
                    String contentType = file.getContentType();

                    // Upload file lên S3
                    try (InputStream inputStream = file.getInputStream()) {
                        ObjectMetadata metadata = new ObjectMetadata();
                        metadata.setContentLength(fileSize);
                        metadata.setContentType(contentType);

                        amazonS3.putObject(bucketName, fileName, inputStream, metadata);

                        String mediaUrl = String.format("https://travle-be.s3.ap-southeast-2.amazonaws.com/%s",
                                fileName);
                        newMediaUrls.add(mediaUrl);

                        String mediaType = (fileName != null && fileName.matches(".*\\.(jpg|jpeg|png|gif|webp)$"))
                                ? "IMAGE"
                                : "VIDEO";

                        boolean mediaExists = false;
                        for (Media existingMedia : currentMedias) {
                            if (existingMedia.getMedia_url().equals(mediaUrl)) {
                                mediaExists = true;
                                break;
                            }
                        }

                        if (!mediaExists) {
                            Media newMedia = new Media(null, post_id, mediaUrl, mediaType);
                            mediaRepository.save(newMedia);
                        }
                    }
                }
            }

            for (Media media : currentMedias) {
                if (!newMediaUrls.contains(media.getMedia_url())) {
                    mediaRepository.delete(media);
                }
            }

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
    public Page<PostResponseDTO> getAllPostTypeVideo(Integer userId, Pageable pageable)
            throws UserAccountNotFoundException {

        User foundUser = userRepository.getAnUser(userId);

        if (foundUser == null) {
            throw new UserAccountNotFoundException("Found user with " + userId + " not found . Please try again !");
        }

        Page<Object[]> results = postRepository.fetchPostWithMediaTypeVideo(userId, pageable);

        return results.map(row -> {
            PostResponseDTO post = new PostResponseDTO();
            post.setOwnerId(((Number) row[0]).intValue());
            post.setPostId(((Number) row[1]).intValue());
            post.setLocationId(((Number) row[2]).intValue());
            post.setLocation((String) row[3]);
            post.setOwnerName((String) row[4]);
            post.setAvatarUrl((String) row[5]);
            post.setPostContent(row[6] != null ? ((String) row[6]) : null);
            post.setStatus((String) row[7]);
            post.setCreate_time((String) row[8]);
            post.setIsShare(((Number) row[9]).intValue());
            post.setUser_share_id(row[10] != null ? ((Number) row[10]).intValue() : null);
            post.setUser_share_name(row[11] != null ? ((String) row[11]) : null);
            post.setUser_share_avatar(row[12] != null ? ((String) row[12]) : null);
            post.setShareContent(row[13] != null ? ((String) row[13]) : null);
            post.setShare_time(row[14] != null ? ((String) row[14]) : null);
            post.setShare_status(row[15] != null ? ((String) row[15]) : null);
            post.setReaction_count(((Number) row[16]).intValue());
            // post.setComment_count(((Number) row[17]).intValue());
            post.setShare_count(((Number) row[18]).intValue());
            post.setUser_reaction_type((String) row[19]);

            Integer comment_count = commentRepository.commentCountWithPostId(((Number) row[1]).intValue());
            post.setComment_count(comment_count == null ? 0 : comment_count);

            Double averageRating = reviewRepository.averageStarWithLocation(((Number) row[2]).intValue());
            post.setAverageRating(averageRating != null ? averageRating : 0.0);

            List<String> medias = mediaRepository.findMediaByPostId(((Number) row[1]).intValue());

            List<MediaResponseDTO> mediaResponseDTOs = medias.stream().map(mediaUrl -> {
                MediaResponseDTO mediaResponseDTO = new MediaResponseDTO();
                mediaResponseDTO.setMediaUrl(mediaUrl);
                mediaResponseDTO.setMediaType(mediaUrl.matches(".*\\.(jpg|jpeg|png|gif|webp)$") ? "IMAGE" : "VIDEO");
                return mediaResponseDTO;
            }).collect(Collectors.toList());

            post.setMediaUrls(mediaResponseDTOs);

            List<String> hashtags = hashTagRespository.findHashtagByPostId(((Number) row[1]).intValue());

            post.setHashtags(hashtags);

            List<Object[]> reactions = postReactionRepository.findTopReactions(((Number) row[1]).intValue());

            if (!reactions.isEmpty()) {
                List<ReactionCountDTO> reactionStatisticsList = new ArrayList<>();

                for (Object[] result : reactions) {
                    ReactionCountDTO reactionStatisticsDTO = new ReactionCountDTO();
                    reactionStatisticsDTO.setType((String) result[0]);
                    reactionStatisticsDTO.setReactionCount(((Number) result[1]).intValue());
                    reactionStatisticsList.add(reactionStatisticsDTO);
                }
                post.setReactionStatistics(reactionStatisticsList);
            } else {
                List<ReactionCountDTO> newLis = new ArrayList<>();
                post.setReactionStatistics(newLis);
            }
            return post;
        });
    }

    public Page<PostMediaDTO> postStatistical(Pageable pageable) {
        Page<Object[]> results = postRepository.statisticalPost(pageable);

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
            // post.setComment_count(((Number) row[11]).intValue());
            post.setShare_count(((Number) row[12]).intValue());

            Integer comment_count = commentRepository.commentCountWithPostId(((Number) row[0]).intValue());
            post.setComment_count(comment_count == null ? 0 : comment_count);

            List<String> hashtags = hashTagRespository.findHashtagByPostId(((Number) row[0]).intValue());

            post.setHashtags(hashtags);

            List<String> medias = mediaRepository.findMediaByPostId(((Number) row[0]).intValue());

            post.setMediaUrls(medias);

            return post;
        });
    }

    public Page<PostResponseDTO> foundPostByUserId(Integer userId, Pageable pageable)
            throws UserAccountNotFoundException {

        User foundUser = userRepository.getAnUser(userId);

        if (foundUser == null) {
            throw new UserAccountNotFoundException("Found user with " + userId + " not found . Please try again !");
        }

        Page<Object[]> results = postRepository.foundPostByUserId(userId, pageable);

        return results.map(row -> {
            PostResponseDTO post = new PostResponseDTO();
            post.setOwnerId(((Number) row[0]).intValue());
            post.setPostId(((Number) row[1]).intValue());
            post.setLocationId(((Number) row[2]).intValue());
            post.setLocation((String) row[3]);
            post.setOwnerName((String) row[4]);
            post.setAvatarUrl((String) row[5]);
            post.setPostContent(row[6] != null ? ((String) row[6]) : null);
            post.setStatus((String) row[7]);
            post.setCreate_time((String) row[8]);
            post.setIsShare(((Number) row[9]).intValue());
            post.setUser_share_id(row[10] != null ? ((Number) row[10]).intValue() : null);
            post.setUser_share_name(row[11] != null ? ((String) row[11]) : null);
            post.setUser_share_avatar(row[12] != null ? ((String) row[12]) : null);
            post.setShareContent(row[13] != null ? ((String) row[13]) : null);
            post.setShare_time(row[14] != null ? ((String) row[14]) : null);
            post.setShare_status(row[15] != null ? ((String) row[15]) : null);
            post.setReaction_count(((Number) row[16]).intValue());
            post.setComment_count(((Number) row[17]).intValue());
            post.setShare_count(((Number) row[18]).intValue());
            post.setUser_reaction_type((String) row[19]);
            post.setShare_post_id(row[20] != null ? ((Number) row[20]).intValue() : null);

            Integer comment_count = commentRepository.commentCountWithPostId(((Number) row[0]).intValue());
            post.setComment_count(comment_count == null ? 0 : comment_count);

            Double averageRating = reviewRepository.averageStarWithLocation(((Number) row[2]).intValue());
            post.setAverageRating(averageRating != null ? averageRating : 0.0);

            List<String> medias;

            if (((Number) row[9]).intValue() == 0) {
                medias = mediaRepository.findMediaByPostId(((Number) row[1]).intValue());
            } else {
                medias = mediaRepository.findMediaByPostId(((Number) row[20]).intValue());
            }

            List<MediaResponseDTO> mediaResponseDTOs = medias.stream().map(mediaUrl -> {
                MediaResponseDTO mediaResponseDTO = new MediaResponseDTO();
                mediaResponseDTO.setMediaUrl(mediaUrl);
                mediaResponseDTO.setMediaType(mediaUrl.matches(".*\\.(jpg|jpeg|png|gif|webp)$") ? "IMAGE" : "VIDEO");
                return mediaResponseDTO;
            }).collect(Collectors.toList());

            post.setMediaUrls(mediaResponseDTOs);

            List<String> hashtags = hashTagRespository.findHashtagByPostId(((Number) row[1]).intValue());

            post.setHashtags(hashtags);
            List<Object[]> reactions = postReactionRepository.findTopReactions(((Number) row[1]).intValue());

            if (!reactions.isEmpty()) {
                List<ReactionCountDTO> reactionStatisticsList = new ArrayList<>();

                for (Object[] result : reactions) {
                    ReactionCountDTO reactionStatisticsDTO = new ReactionCountDTO();
                    reactionStatisticsDTO.setType((String) result[0]);
                    reactionStatisticsDTO.setReactionCount(((Number) result[1]).intValue());
                    reactionStatisticsList.add(reactionStatisticsDTO);
                }
                post.setReactionStatistics(reactionStatisticsList);
            } else {
                List<ReactionCountDTO> newLis = new ArrayList<>();
                post.setReactionStatistics(newLis);
            }
            return post;
        });
    }

    public List<String> getAllHashTag() {
        return hashTagRespository.findHashtag();
    }

    public List<PostStatisticalDTO> getPostCreateCount(int year) {
        List<Object[]> results = postRepository.PostCreateInMonth(year);

        List<PostStatisticalDTO> postStatisticalDTOs = new ArrayList<>();

        for (Object[] row : results) {
            PostStatisticalDTO dto = new PostStatisticalDTO();
            dto.setMonth(((Number) row[0]).intValue());
            dto.setPost_count(((Number) row[1]).intValue());
            postStatisticalDTOs.add(dto);
        }
        return postStatisticalDTOs;
    }
}
