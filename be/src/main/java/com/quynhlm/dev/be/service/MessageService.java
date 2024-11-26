package com.quynhlm.dev.be.service;

import java.sql.Timestamp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.quynhlm.dev.be.core.exception.UnknownException;
import com.quynhlm.dev.be.model.dto.requestDTO.MessageRequestDTO;
import com.quynhlm.dev.be.model.entity.Message;
import com.quynhlm.dev.be.model.entity.MessageStatus;
import com.quynhlm.dev.be.repositories.MessageRepository;
import com.quynhlm.dev.be.repositories.MessageStatusRepositoty;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MessageService {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private MessageStatusRepositoty messageStatusRepositoty;

    public Page<Message> getAllListData(Integer group_id, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return messageRepository.findAllMessageGroup(group_id, pageable);
    }

    public void insertMessage(MessageRequestDTO messageRequestDTO) throws UnknownException {
        try {
            messageRequestDTO.getMessage().setSendTime(new Timestamp(System.currentTimeMillis()).toString());
            isSuccess(messageRequestDTO.getMessage() , messageRequestDTO.getStatus());
        } catch (Exception e) {
            throw new UnknownException(e.getMessage());
        }
    }

    public void isSuccess(Message message , Boolean status) {
        Message saveMessage = messageRepository.save(message);
        if (saveMessage.getId() == null) {
            throw new UnknownException("Transaction cannot be completed!");
        }
        MessageStatus messageStatus = new MessageStatus();
        messageStatus.setUserId(saveMessage.getUserSendId());
        messageStatus.setMessageId(saveMessage.getId());
        messageStatus.setStatus(status);
        messageStatusRepositoty.save(messageStatus);
    }
}
