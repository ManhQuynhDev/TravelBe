package com.quynhlm.dev.be.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.quynhlm.dev.be.core.exception.UnknownException;
import com.quynhlm.dev.be.model.dto.responseDTO.UserMessageResponseDTO;
import com.quynhlm.dev.be.model.entity.Message;
import com.quynhlm.dev.be.repositories.MessageRepository;

@Service
public class MessageService {

    @Autowired
    private AmazonS3 amazonS3;

    @Value("${aws.s3.bucketName}")
    private String bucketName;

    @Autowired
    private MessageRepository messageRepository;

    public void changeStatusMessage(Integer messageId) {
        Message foundMessage = messageRepository.findByMessageId(messageId);

        if (foundMessage != null) {
            foundMessage.setStatus(true);

            messageRepository.save(foundMessage);
        }
    }

    public void updateMessage(Integer messageId, String content) {
        Message foundMessage = messageRepository.findByMessageId(messageId);

        if (foundMessage != null) {
            foundMessage.setContent(content);

            messageRepository.save(foundMessage);
        }
    }

    public void deleteMessage(Integer messageId) {
        Message foundMessage = messageRepository.findByMessageId(messageId);

        if (foundMessage != null) {
            messageRepository.delete(foundMessage);
        }
    }

    public UserMessageResponseDTO sendMessage(Message message) {
        message.setSendTime(new Timestamp(System.currentTimeMillis()).toString());
        message.setStatus(false);
        String mediaUrl = null;
        if (message.getMediaUrl() != null && !message.getMediaUrl().isEmpty()) {
            mediaUrl = handleFileUpload(message.getMediaUrl());
        }
        message.setMediaUrl(mediaUrl);
        Message saveMessage = messageRepository.save(message);
        if (saveMessage == null) {
            throw new UnknownException("Transaction cannot be completed!");
        }
        return getAnMessage(saveMessage.getId());
    }

    private String handleFileUpload(String fileBase64) {
        try {
            String[] parts = fileBase64.split(",");
            String header = parts[0];
            String fileData = parts[1];

            String fileType = header.split(";")[0].split(":")[1];

            if (!fileType.startsWith("image/")) {
                throw new IllegalArgumentException("Invalid file type. Only images are allowed.");
            }

            byte[] decodedBytes = Base64.getDecoder().decode(fileData);

            String fileName = UUID.randomUUID().toString() + "." + fileType.split("/")[1];

            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(decodedBytes.length);
            metadata.setContentType(fileType);

            try (InputStream inputStream = new ByteArrayInputStream(decodedBytes)) {
                amazonS3.putObject(bucketName, fileName, inputStream, metadata);
            }
            return String.format("https://%s.s3.amazonaws.com/%s", bucketName, fileName);
        } catch (IOException e) {
            throw new UnknownException("Error handling Base64 file: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new UnknownException("Invalid file type: " + e.getMessage());
        }
    }

    public UserMessageResponseDTO getAnMessage(Integer id) {
        List<Object[]> results = messageRepository.findAnMessage(id);

        Object[] result = results.get(0);

        Integer message_id = ((Number) result[0]).intValue();
        Integer sender_id = ((Number) result[1]).intValue();
        Integer receiver_id = ((Number) result[2]).intValue();
        String content = (String) result[3];
        String fullname = (String) result[4];
        String avatar = (String) result[5];
        String mediaUrl = (String) result[6];
        Boolean status = (Boolean) result[7];
        String send_time = (String) result[8];

        return new UserMessageResponseDTO(message_id, sender_id, receiver_id, content, fullname, avatar, mediaUrl,
                status,
                send_time);
    }

    public Page<UserMessageResponseDTO> getAllMessageUser(Integer senderId, Integer receiverId, Pageable pageable) {
        Page<Object[]> results = messageRepository.getAllMessageWithTwoUser(senderId, receiverId, pageable);

        return results.map(row -> {
            Integer message_id = ((Number) row[0]).intValue();
            Integer sender_id = ((Number) row[1]).intValue();
            Integer receiver_id = ((Number) row[2]).intValue();
            String content = (String) row[3];
            String fullname = (String) row[4];
            String avatar = (String) row[5];
            String mediaUrl = (String) row[6];
            Boolean status = (Boolean) row[7];
            String send_time = (String) row[8];
            return new UserMessageResponseDTO(message_id, sender_id, receiver_id, content, fullname, avatar, mediaUrl,
                    status,
                    send_time);
        });
    }
}