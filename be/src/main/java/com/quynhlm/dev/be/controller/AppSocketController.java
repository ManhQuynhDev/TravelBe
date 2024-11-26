package com.quynhlm.dev.be.controller;

import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.quynhlm.dev.be.core.exception.GroupNotFoundException;
import com.quynhlm.dev.be.model.dto.requestDTO.MessageRequestDTO;
import com.quynhlm.dev.be.model.entity.Group;
import com.quynhlm.dev.be.model.entity.MessageStatus;
import com.quynhlm.dev.be.repositories.GroupRepository;
import com.quynhlm.dev.be.repositories.MessageStatusRepositoty;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.corundumstudio.socketio.SocketIONamespace;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.listener.ConnectListener;
import com.corundumstudio.socketio.listener.DataListener;
import com.corundumstudio.socketio.listener.DisconnectListener;

@Component
public class AppSocketController {

    @Autowired
    private MessageStatusRepositoty messageStatusRepositoty;

    @Autowired
    private GroupRepository groupRepository;

    private SocketIONamespace namespace;

    private ConcurrentHashMap<String, Boolean> userTypingMap = new ConcurrentHashMap<>();

    private ConcurrentHashMap<String, String> userSocketMap = new ConcurrentHashMap<>();

    @Autowired
    public AppSocketController(SocketIOServer server) {
        this.namespace = server.addNamespace("/chat");

        this.namespace.addConnectListener(onConnectListener);
        this.namespace.addDisconnectListener(onDisconnectListener);
        this.namespace.addEventListener("onChat", MessageRequestDTO.class, onSendMessage);
        this.namespace.addEventListener("userTyping", String.class, onUserTyping);
        this.namespace.addEventListener("onMessageRead", MessageRequestDTO.class, messageRead);
        this.namespace.addEventListener("userStoppedTyping", String.class, onUserStoppedTyping);
    }

    public ConnectListener onConnectListener = client -> {
        System.out.println("User " + client.getSessionId() + " connected");

        String room = client.getHandshakeData().getSingleUrlParam("room");
        if (room != null && !room.isEmpty()) {
            Group foundGroup = groupRepository.findGroupById(Integer.parseInt(room));

            if (foundGroup == null) {
                client.disconnect();
                throw new GroupNotFoundException("Room " + room + " not found");
            }

            client.joinRoom(room);
            System.out.println("User " + client.getSessionId() + " joined room: " + room);
        }

        String userId = client.getHandshakeData().getSingleUrlParam("userId");
        if (userId != null && !userId.isEmpty()) {
            userSocketMap.put(userId, client.getSessionId().toString());
            System.out.println("User " + userId + " connected with socket ID: " + client.getSessionId());
        } else {
            System.out.println("User connected without a valid userId.");
        }
    };

    public DisconnectListener onDisconnectListener = client -> {
        System.out.println("User " + client.getSessionId() + " disconnected");

        String userId = getUserIdBySocketId(client.getSessionId().toString());
        if (userId != null) {
            userSocketMap.remove(userId);
            userTypingMap.remove(userId);
            System.out.println("User " + userId + " removed from socket map.");
        }
    };

    public DataListener<MessageRequestDTO> onSendMessage = (client, messageRequestDTO, ackRequest) -> {
        try {
            String room = messageRequestDTO.getMessage().getGroupId().toString();
            String message = messageRequestDTO.getMessage().getContent();

            if (room != null && !room.isEmpty()) {
                namespace.getRoomOperations(room).sendEvent("onChat", messageRequestDTO);
                System.out.println("Message sent to room " + room + ": " + message);
            } else {
                System.out.println("Room ID is invalid.");
            }
        } catch (Exception e) {
            System.err.println("Error sending message: " + e.getMessage());
            e.printStackTrace();
        }
    };

    @Setter
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor

    class MessageReadDTO {
        private int messageId;
    private String room;
    private String status;
    }

    public DataListener<MessageRequestDTO> messageRead = (client, messageRequestDTO, ackRequest) -> {
        try {
            boolean isRead = messageRequestDTO.getStatus(); // Trạng thái tin nhắn (true = đã xem, false = chưa xem)
            
            // Lấy thông tin phòng và tin nhắn từ messageRequestDTO
            String room = messageRequestDTO.getMessage().getGroupId().toString();
            int messageId = messageRequestDTO.getMessage().getId();
            
            // Tìm trạng thái tin nhắn từ database
            MessageStatus foundMessage = messageStatusRepositoty.getAnMessageStatusWithUserId(messageRequestDTO.getMessage().getUserSendId());
    
            if (foundMessage != null) {
                System.out.println("Received status: " + isRead);
                System.out.println("Room ID: " + room);
                System.out.println("Message ID: " + messageId);
    
                // Cập nhật trạng thái của tin nhắn trong cơ sở dữ liệu
                foundMessage.setStatus(isRead); // Cập nhật trạng thái
                MessageStatus isChecker = messageStatusRepositoty.save(foundMessage);
    
                if (isChecker.getId() != null) {
                    // Gửi sự kiện "onMessageRead" với thông tin tin nhắn và trạng thái
                    namespace.getRoomOperations(room).sendEvent("onMessageRead", 
                        new MessageReadDTO(messageId, room, isRead ? "Đã xem" : "Chưa xem"));
                    
                    System.out.println("Message marked as " + (isRead ? "read" : "unread") + " in room " + room);
                } else {
                    System.out.println("Failed to update message status in database.");
                }
            } else {
                System.out.println("Message not found in database.");
            }
            
        } catch (Exception e) {
            System.err.println("Error sending message read status: " + e.getMessage());
            e.printStackTrace();
        }
    };
    
    
    public DataListener<String> onUserTyping = (client, user_id, ackRequest) -> {
        String room = client.getHandshakeData().getSingleUrlParam("room");
        if (room != null && !room.isEmpty()) {
            userTypingMap.put(user_id, true);
            namespace.getRoomOperations(room).sendEvent("typingUsers", userTypingMap);
        }
    };

    public DataListener<String> onUserStoppedTyping = (client, user_id, ackRequest) -> {
        String room = client.getHandshakeData().getSingleUrlParam("room");
        if (room != null && !room.isEmpty()) {
            userTypingMap.remove(user_id);
            namespace.getRoomOperations(room).sendEvent("typingUsers", userTypingMap);
        }
    };

    private String getUserIdBySocketId(String socketId) {
        for (String userId : userSocketMap.keySet()) {
            if (userSocketMap.get(userId).equals(socketId)) {
                return userId;
            }
        }
        return null;
    }
}
