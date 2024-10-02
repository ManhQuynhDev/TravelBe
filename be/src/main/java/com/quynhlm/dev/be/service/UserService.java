package com.quynhlm.dev.be.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.quynhlm.dev.be.core.exception.UnknowException;
import com.quynhlm.dev.be.core.exception.UserAccountExistingException;
import com.quynhlm.dev.be.model.User;
import com.quynhlm.dev.be.repositories.UserRepository;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public void login() {

    }

    public void register(User user) throws UserAccountExistingException, UnknowException {
        checkUserExists(user);

        User saveUser = userRepository.save(user);
        if (saveUser.getUser_id() == null) {
            throw new UnknowException("Transaction cannot complete!");
        }
    }

    private void checkUserExists(User user) throws UserAccountExistingException {
        if (!userRepository.findByEmail(user.getEmail()).isEmpty()) {
            throw new UserAccountExistingException("Email " + user.getEmail() + " already exist. Please try another!");
        }
        if (!userRepository.findByUsername(user.getUsername()).isEmpty()) {
            throw new UserAccountExistingException(
                    "Username " + user.getUsername() + " already exist. Please try another!");
        }
        if (!userRepository.findByPhoneNumber(user.getPhoneNumber()).isEmpty()) {
            throw new UserAccountExistingException(
                    "PhoneNumber " + user.getPhoneNumber() + " already exist. Please try another!");
        }
    }
}
