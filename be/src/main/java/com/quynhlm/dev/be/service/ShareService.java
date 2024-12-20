package com.quynhlm.dev.be.service;

import java.sql.Timestamp;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.quynhlm.dev.be.core.exception.PostNotFoundException;
import com.quynhlm.dev.be.core.exception.ShareNotFoundException;
import com.quynhlm.dev.be.core.exception.UnknownException;
import com.quynhlm.dev.be.core.exception.UserAccountNotFoundException;
import com.quynhlm.dev.be.model.dto.responseDTO.PostResponseDTO;
import com.quynhlm.dev.be.model.dto.responseDTO.UserTagPostResponse;
import com.quynhlm.dev.be.model.entity.Post;
import com.quynhlm.dev.be.model.entity.Share;
import com.quynhlm.dev.be.model.entity.User;
import com.quynhlm.dev.be.repositories.HashTagRespository;
import com.quynhlm.dev.be.repositories.MediaRepository;
import com.quynhlm.dev.be.repositories.PostRepository;
import com.quynhlm.dev.be.repositories.ReviewRepository;
import com.quynhlm.dev.be.repositories.ShareRepository;
import com.quynhlm.dev.be.repositories.TagRepository;
import com.quynhlm.dev.be.repositories.UserRepository;

@Service
public class ShareService {
    @Autowired
    private ShareRepository shareRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private HashTagRespository hashTagRespository;

    @Autowired
    private MediaRepository mediaRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private UserRepository userRepository;

    public PostResponseDTO sharePost(Share share)
            throws PostNotFoundException, UserAccountNotFoundException, ShareNotFoundException {
        Post foundPost = postRepository.getAnPost(share.getPostId());

        if (foundPost == null) {
            throw new PostNotFoundException(
                    "Post find with id " + share.getPostId() + " not found. Please try another!");
        }

        if (foundPost.getUser_id() == share.getUserId()) {
            throw new ShareNotFoundException("You cannot share your post");
        }

        User foundUser = userRepository.getAnUser(share.getUserId());

        if (foundUser == null) {
            throw new UserAccountNotFoundException(
                    "User find with id " + share.getUserId() + " not found. Please try another!");
        }

        share.setCreate_time(new Timestamp(System.currentTimeMillis()).toString());

        Share saveShare = shareRepository.save(share);
        if (saveShare.getId() == null) {
            throw new UnknownException("Transaction cannot be completed!");
        }
        return getAnShare(share.getId(), saveShare.getUserId());
    }

    public void deleteShare(int id) throws ShareNotFoundException {
        Share foundShare = shareRepository.getAnShare(id);
        if (foundShare == null) {
            throw new ShareNotFoundException("Share find with id " + id + " not found. Please try another!");
        }
        shareRepository.delete(foundShare);
    }

    public PostResponseDTO getAnShare(Integer shareId, Integer user_id) throws ShareNotFoundException {
        List<Object[]> results = shareRepository.getAnShareWithID(shareId, user_id);

        if (results.isEmpty()) {
            throw new ShareNotFoundException(
                    "Id " + shareId + " not found or invalid data. Please try another!");
        }
        Object[] row = results.get(0);
        PostResponseDTO post = new PostResponseDTO();

        post.setOwnerId(((Number) row[0]).intValue());
        post.setPostId(((Number) row[1]).intValue());
        post.setLocationId(((Number) row[2]).intValue());
        post.setLocation((String) row[3]);
        post.setOwnerName((String) row[4]);
        post.setAvatarUrl((String) row[5]);
        post.setShareContent(row[6] != null ? ((String) row[6]) : null);
        post.setShareContent(row[7] != null ? ((String) row[7]) : null);
        post.setStatus((String) row[8]);
        post.setType((String) row[9]);
        post.setIsShare(((Number) row[10]).intValue());
        post.setCreate_time((String) row[11]);
        post.setShare_by_user(row[12] != null ? ((Number) row[12]).intValue() : null);
        post.setShare_by_name(row[13] != null ? ((String) row[13]) : null);
        post.setShare_by_avatar(row[14] != null ? ((String) row[14]) : null);
        post.setReaction_count(((Number) row[15]).intValue());
        post.setComment_count(((Number) row[16]).intValue());
        post.setShare_count(((Number) row[17]).intValue());
        post.setIsTag(((Number) row[18]).intValue());
        post.setUser_reaction_type((String) row[19]);

        Double averageRating = reviewRepository.averageStarWithLocation(((Number) row[2]).intValue());
        post.setAverageRating(averageRating != null ? averageRating : 0.0);

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
    }
}
