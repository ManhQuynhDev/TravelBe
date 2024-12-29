package com.quynhlm.dev.be.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.quynhlm.dev.be.model.entity.User;
import com.quynhlm.dev.be.repositories.UserRepository;

@Component
public class UnlockUserTask {
    @Autowired
    private UserRepository userRepository;

    @Scheduled(cron = "0 0 0 * * ?") 
    public void unlockUsers() {
        LocalDateTime threeDaysAgo = LocalDateTime.now().minusDays(3);
        List<User> lockedUsers = userRepository.findAllByIsLockedAndLockDateBefore("LOCK", threeDaysAgo);

        for (User user : lockedUsers) {
            user.setIsLocked("OPEN");
            user.setLockDate(null);
            user.setTermDate(null);
            userRepository.save(user);
        }
    }
}
