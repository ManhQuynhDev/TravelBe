package com.quynhlm.dev.be.service;

import java.sql.Timestamp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.quynhlm.dev.be.core.exception.PostNotFoundException;
import com.quynhlm.dev.be.core.exception.TagNotFoundException;
import com.quynhlm.dev.be.core.exception.UnknownException;
import com.quynhlm.dev.be.core.exception.UserAccountNotFoundException;
import com.quynhlm.dev.be.model.entity.Post;
import com.quynhlm.dev.be.model.entity.Tag;
import com.quynhlm.dev.be.model.entity.User;
import com.quynhlm.dev.be.repositories.PostRepository;
import com.quynhlm.dev.be.repositories.TagRepository;
import com.quynhlm.dev.be.repositories.UserRepository;

@Service
public class TagService {
    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;
    // Add Tag

    public void tagWithFriend(Tag tag) throws PostNotFoundException, UserAccountNotFoundException, UnknownException {
        User foundUser = userRepository.getAnUser(tag.getUserId());
        if (foundUser == null) {
            throw new UserAccountNotFoundException(
                    "Found user with with " + tag.getUserId() + " not found, please try again!");
        }

        Post foundPost = postRepository.getAnPost(tag.getPostId());
        if (foundPost == null) {
            throw new PostNotFoundException(
                    "Found post with with " + tag.getUserId() + " not found, please try again!");
        }

        tag.setCreate_time(new Timestamp(System.currentTimeMillis()).toString());

        Tag saveTag = tagRepository.save(tag);
        if (saveTag.getId() == null) {
            throw new UnknownException("Transaction cannot complete!");
        }
    }

    public void deleteUserFormTag(Integer id, Integer userId) throws TagNotFoundException {
        Tag foundTag = tagRepository.foundTagWithIdAndUserId(id, userId);
        if (foundTag == null) {
            throw new TagNotFoundException(
                    "Found tag with userId " + userId + " not found, please try again!");
        }
        tagRepository.delete(foundTag);
    }
}
