// package com.quynhlm.dev.be.service;

// import java.io.ByteArrayInputStream;
// import java.io.IOException;
// import java.io.InputStream;
// import java.sql.Timestamp;
// import java.util.Base64;
// import java.util.UUID;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.beans.factory.annotation.Value;
// import org.springframework.data.domain.Page;
// import org.springframework.data.domain.PageRequest;
// import org.springframework.data.domain.Pageable;
// import org.springframework.stereotype.Service;

// import com.amazonaws.services.s3.AmazonS3;
// import com.amazonaws.services.s3.model.ObjectMetadata;
// import com.quynhlm.dev.be.core.exception.UnknownException;
// import com.quynhlm.dev.be.model.dto.requestDTO.MessageRequestDTO;
// import com.quynhlm.dev.be.model.dto.responseDTO.MessageResponseDTO;
// import com.quynhlm.dev.be.model.entity.Message;
// import com.quynhlm.dev.be.model.entity.MessageStatus;
// import com.quynhlm.dev.be.repositories.MessageRepository;
// import com.quynhlm.dev.be.repositories.MessageStatusRepositoty;

// import lombok.RequiredArgsConstructor;

// @Service
// @RequiredArgsConstructor
// public class MessageService {

//     @Autowired
//     private MessageRepository messageRepository;

//     @Autowired
//     private AmazonS3 amazonS3;

//     @Value("${aws.s3.bucketName}")
//     private String bucketName;

//     @Autowired
//     private MessageStatusRepositoty messageStatusRepositoty;

//     public Page<Message> getAllListData(Integer group_id, int page, int size) {
//         Pageable pageable = PageRequest.of(page, size);
//         return messageRepository.findAllMessageGroup(group_id, pageable);
//     }

//     // public MessageResponseDTO sendMessage(MessageRequestDTO messageRequestDTO) {
//     //     try {
//     //         // Đặt thời gian gửi tin nhắn
//     //         messageRequestDTO.getMessage().setSendTime(new Timestamp(System.currentTimeMillis()).toString());

//     //         // Xử lý file (nếu có)
//     //         String mediaUrl = null;
//     //         if (messageRequestDTO.getFile() != null && !messageRequestDTO.getFile().isEmpty()) {
//     //             // Xử lý việc lưu file base64 và upload lên S3
//     //             mediaUrl = handleFileUpload(messageRequestDTO.getFile());
//     //         }

//     //         return isSuccess(messageRequestDTO.getMessage(), messageRequestDTO.getStatus(), mediaUrl);
//     //     } catch (Exception e) {
//     //         // Xử lý lỗi
//     //         throw new UnknownException("Error sending message: " + e.getMessage());
//     //     }
//     // }

//     private String handleFileUpload(String fileBase64) {
//         try {
//             String[] parts = fileBase64.split(",");
//             String header = parts[0]; 
//             String fileData = parts[1]; 
    
//             String fileType = header.split(";")[0].split(":")[1];
            
//             if (!fileType.startsWith("image/")) {
//                 throw new IllegalArgumentException("Invalid file type. Only images are allowed.");
//             }
    
//             byte[] decodedBytes = Base64.getDecoder().decode(fileData);
    
//             String fileName = UUID.randomUUID().toString() + "." + fileType.split("/")[1];
    
//             ObjectMetadata metadata = new ObjectMetadata();
//             metadata.setContentLength(decodedBytes.length);
//             metadata.setContentType(fileType);  
       
//             try (InputStream inputStream = new ByteArrayInputStream(decodedBytes)) {
//                 amazonS3.putObject(bucketName, fileName, inputStream, metadata);
//             }
//             return String.format("https://%s.s3.amazonaws.com/%s", bucketName, fileName);
//         } catch (IOException e) {
//             throw new UnknownException("Error handling Base64 file: " + e.getMessage());
//         } catch (IllegalArgumentException e) {
//             throw new UnknownException("Invalid file type: " + e.getMessage());
//         }
//     }
    

//     // public MessageResponseDTO isSuccess(Message message, Boolean status, String mediaUrl) {
//     //     try {
//     //         // Lưu tin nhắn vào cơ sở dữ liệu
//     //         Message saveMessage = messageRepository.save(message);
//     //         if (saveMessage.getId() == null) {
//     //             throw new UnknownException("Transaction cannot be completed!");
//     //         }

//     //         // Lưu trạng thái tin nhắn
//     //         MessageStatus messageStatus = new MessageStatus();
//     //         messageStatus.setUserId(saveMessage.getUserSendId());
//     //         messageStatus.setMessageId(saveMessage.getId());
//     //         messageStatus.setStatus(status);
//     //         messageStatusRepositoty.save(messageStatus);

//     //         return new MessageResponseDTO(saveMessage, status, mediaUrl);
//     //     } catch (Exception e) {
//     //         // Xử lý lỗi trong quá trình lưu
//     //         throw new UnknownException("Error saving message: " + e.getMessage());
//     //     }
//     // }
// }
