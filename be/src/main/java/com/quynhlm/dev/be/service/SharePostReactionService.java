package com.quynhlm.dev.be.service;

import java.sql.Timestamp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.quynhlm.dev.be.core.exception.PostNotFoundException;
import com.quynhlm.dev.be.core.exception.ShareNotFoundException;
import com.quynhlm.dev.be.core.exception.UnknownException;
import com.quynhlm.dev.be.core.exception.UserAccountNotFoundException;
import com.quynhlm.dev.be.model.entity.Share;
import com.quynhlm.dev.be.model.entity.SharePostReaction;
import com.quynhlm.dev.be.model.entity.User;
import com.quynhlm.dev.be.repositories.SharePostReactionRepository;
import com.quynhlm.dev.be.repositories.ShareRepository;
import com.quynhlm.dev.be.repositories.UserRepository;

@Service
public class SharePostReactionService {
    @Autowired
    private SharePostReactionRepository sharePostReactionRepository;

    @Autowired
    private ShareRepository shareRepository;

    @Autowired
    private UserRepository userRepository;

    public void updateReaction(SharePostReaction shareReaction)
            throws PostNotFoundException, UserAccountNotFoundException {

        Share foundShare = shareRepository.getAnShare(shareReaction.getShareId());

        if (foundShare == null) {
            throw new ShareNotFoundException(
                    "Share post find with id " + shareReaction.getShareId() + " not found. Please try another!");
        }

        User foundUser = userRepository.getAnUser(shareReaction.getUserId());

        if (foundUser == null) {
            throw new UserAccountNotFoundException(
                    "User find with id " + shareReaction.getUserId() + " not found. Please try another!");
        }

        SharePostReaction foundReaction = sharePostReactionRepository.findByShareIdAndUserId(shareReaction.getShareId(),
                shareReaction.getUserId());
        if (foundReaction != null) {
            if (foundReaction.getType() == shareReaction.getType()) {
                sharePostReactionRepository.delete(foundReaction);
            } else {
                foundReaction.setType(shareReaction.getType());
                isSuccess(foundReaction);
            }
        } else {
            SharePostReaction newReaction = new SharePostReaction();
            newReaction.setType(shareReaction.getType());
            newReaction.setShareId(shareReaction.getShareId());
            newReaction.setUserId(shareReaction.getUserId());
            newReaction.setCreate_time(new Timestamp(System.currentTimeMillis()).toString());
            isSuccess(newReaction);
        }
    }

    public void isSuccess(SharePostReaction sharePostReaction) throws UnknownException {
        SharePostReaction saveReaction = sharePostReactionRepository.save(sharePostReaction);
        if (saveReaction.getId() == null) {
            throw new UnknownException("Transaction cannot be completed!");
        }
    }
}
