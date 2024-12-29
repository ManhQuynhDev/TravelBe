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
        return null;
    }
}
