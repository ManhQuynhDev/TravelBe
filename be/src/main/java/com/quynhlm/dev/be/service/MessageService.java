package com.quynhlm.dev.be.service;

import java.sql.Timestamp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.quynhlm.dev.be.core.exception.UnknownException;
import com.quynhlm.dev.be.model.entity.Message;
import com.quynhlm.dev.be.repositories.MessageRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MessageService {

    // @Autowired
    // private AmazonS3 amazonS3;

    @Value("${aws.s3.bucketName}")
    private String bucketName;

    @Autowired
    private MessageRepository messageRepository;

    public void insertMessage(Message message) throws UnknownException {
        try {
            message.setSendTime(new Timestamp(System.currentTimeMillis()).toString());
            isSuccess(message);
        } catch (Exception e) {
            throw new UnknownException(e.getMessage());
        }
    }

    public void isSuccess(Message message) {
        Message saveMessage = messageRepository.save(message);
        if (saveMessage.getId() == null) {
            throw new UnknownException("Transaction cannot be completed!");
        }
    }

}
