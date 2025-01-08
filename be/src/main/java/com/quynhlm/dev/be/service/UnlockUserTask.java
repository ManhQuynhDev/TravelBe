package com.quynhlm.dev.be.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.quynhlm.dev.be.model.entity.User;
import com.quynhlm.dev.be.repositories.UserRepository;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class UnlockUserTask {
    @Autowired
    private UserRepository userRepository;

    @Scheduled(cron = "0 0 0 * * ?", zone = "Asia/Ho_Chi_Minh")
    public void unlockUsers() {
        LocalDateTime now = LocalDateTime.now();
        String date = now.toString().substring(0,9);
        List<User> lockedUsers = userRepository.findAllByIsLockedAndLockDateBefore("LOCK", date);
        log.info(lockedUsers.size() + "");
        for (User user : lockedUsers) {
            user.setIsLocked("OPEN");
            user.setLockDate(null);
            user.setTermDate(null);
            userRepository.save(user);
        }
    }
}
