package com.quynhlm.dev.be.controller;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.corundumstudio.socketio.SocketIONamespace;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.listener.ConnectListener;
import com.corundumstudio.socketio.listener.DataListener;
import com.corundumstudio.socketio.listener.DisconnectListener;
import com.quynhlm.dev.be.model.dto.requestDTO.IconDTO;
import com.quynhlm.dev.be.model.dto.requestDTO.MessageSeenDTO;
import com.quynhlm.dev.be.model.dto.responseDTO.MessageDTO;
import com.quynhlm.dev.be.model.dto.responseDTO.UserMessageResponseDTO;
import com.quynhlm.dev.be.model.entity.Message;
import com.quynhlm.dev.be.model.entity.Notification;
import com.quynhlm.dev.be.service.MessageService;
import com.quynhlm.dev.be.service.NotificationService;
import com.quynhlm.dev.be.service.UserService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class MessageSocketController {

    @Autowired
    private MessageService messageService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserService userService;

    private SocketIONamespace namespace;

    public Map<String, Map<String, Boolean>> messageStatus = new HashMap<>();

    private ConcurrentHashMap<String, Boolean> userTypingMap = new ConcurrentHashMap<>();

    private ConcurrentHashMap<String, String> userSocketMap = new ConcurrentHashMap<>();

    @Autowired
    public MessageSocketController(SocketIOServer server) {
        this.namespace = server.addNamespace("/private");

        this.namespace.addConnectListener(onConnectListener);
        this.namespace.addDisconnectListener(onDisconnectListener);
        this.namespace.addEventListener("send-message", MessageDTO.class, onSendMessage);
        this.namespace.addEventListener("message-seen", MessageSeenDTO.class, onMessageSeen);
        this.namespace.addEventListener("insert-icon", IconDTO.class, onAddIconToMessage);
        this.namespace.addEventListener("userTyping", String.class, onUserTyping);
        this.namespace.addEventListener("userStoppedTyping", String.class, onUserStoppedTyping);
    }

    public static String generateRoom(String input1, String input2) {
        String[] inputs = { input1, input2 };

        Arrays.sort(inputs);

        return String.join("-", inputs);
    }

    public ConnectListener onConnectListener = client -> {

        System.out.println("User " + client.getSessionId() + " connected");
        String senderId = client.getHandshakeData().getSingleUrlParam("sender");
        String receiverId = client.getHandshakeData().getSingleUrlParam("receiverId");

        String room = generateRoom(senderId, receiverId);
        if (room != null && !room.isEmpty()) {
            client.joinRoom(room);
            System.out.println("User " + client.getSessionId() + " joined room: " + room);
        }
    };

    public DisconnectListener onDisconnectListener = client -> {
        String senderId = client.getHandshakeData().getSingleUrlParam("sender");
        if (senderId != null) {
            userSocketMap.remove(senderId);
            userTypingMap.remove(senderId);
            System.out.println("User " + senderId + " removed from socket map.");
        }
        System.out.println("User " + client.getSessionId() + " disconnected");
    };

    public DataListener<String> onUserTyping = (client, user_id, ackRequest) -> {
        String senderId = client.getHandshakeData().getSingleUrlParam("sender");
        String receiverId = client.getHandshakeData().getSingleUrlParam("receiverId");

        String room = generateRoom(senderId, receiverId);
        if (room != null && !room.isEmpty()) {
            String name = userService.getUserFullname(Integer.parseInt(user_id));
            System.out.println("Fullname :" + name);
            userTypingMap.put(name, true);
            namespace.getRoomOperations(room).sendEvent("typingUsers", userTypingMap);
        }
    };

    public DataListener<String> onUserStoppedTyping = (client, user_id, ackRequest) -> {
        String senderId = client.getHandshakeData().getSingleUrlParam("sender");
        String receiverId = client.getHandshakeData().getSingleUrlParam("receiverId");

        String room = generateRoom(senderId, receiverId);

        if (room != null && !room.isEmpty()) {
            String name = userService.getUserFullname(Integer.parseInt(user_id));
            userTypingMap.remove(name);
            namespace.getRoomOperations(room).sendEvent("typingUsers", userTypingMap);
        }
    };

    public DataListener<MessageDTO> onSendMessage = (client, data, ackRequest) -> {
        try {
            String senderId = client.getHandshakeData().getSingleUrlParam("sender");
            String receiverId = client.getHandshakeData().getSingleUrlParam("receiverId");

            String room = generateRoom(senderId, receiverId);

            Message saveMessage = new Message();
            saveMessage.setContent(data.getMessage());
            saveMessage.setSenderId(data.getSender());
            saveMessage.setReceiverId(data.getReceiver());
            saveMessage.setMediaUrl(data.getFile());

            Notification notification = new Notification();
            String fullname = userService.getUserFullname(saveMessage.getSenderId());
            notification.setTitle(fullname);

            UserMessageResponseDTO result = messageService.sendMessage(saveMessage);

            if (result.getMediaUrl() != null) {
                notification.setMediaUrl(result.getMediaUrl());
            }
            
            notification.setUserSendId(saveMessage.getSenderId());
            notification.setMessage(saveMessage.getContent());
            notification.setUserReceivedId(saveMessage.getReceiverId());

            Notification response = notificationService.saveNotification(notification);

            

            this.namespace.getRoomOperations(room).sendEvent("notification", response);

            this.namespace.getRoomOperations(room).sendEvent("user-chat", result);
        } catch (Exception e) {
            System.err.println("Error in onSendMessage event: " + e.getMessage());
            e.printStackTrace();
        }
    };

    public DataListener<IconDTO> onAddIconToMessage = (client, data, ackRequest) -> {
        try {
            String roomId = generateRoom(data.getSender(), data.getReceiver());

            System.out.println("Room :" + roomId);

            UserMessageResponseDTO messageResponseDTO = messageService.updateMessage(data);

            this.namespace.getRoomOperations(roomId).sendEvent("user-chat-update", messageResponseDTO);
        } catch (Exception e) {
            System.err.println("Error in onAddIconToMessage event: " + e.getMessage());
            e.printStackTrace();
        }
    };

    public DataListener<MessageSeenDTO> onMessageSeen = (client, messageSeenDTO, ackRequest) -> {
        try {

            String getSenderId = client.getHandshakeData().getSingleUrlParam("sender");
            String getReceiverId = client.getHandshakeData().getSingleUrlParam("receiverId");

            String room = generateRoom(getSenderId, getReceiverId);

            String viewerId = messageSeenDTO.getViewerId();
            String senderId = messageSeenDTO.getSenderId();
            String messageId = messageSeenDTO.getMessageId();

            if (room != null && !room.isEmpty()) {
                System.out.println("SenderId :" + senderId);
                System.out.println("ViewerID :" + viewerId);

                if (viewerId.equals(senderId)) {
                    System.out.println("User " + viewerId + " is the sender, skipping adding to seen list.");
                    return;
                }

                messageStatus.computeIfAbsent(room, k -> new HashMap<>()).put(viewerId, true);

                this.namespace.getRoomOperations(room).sendEvent("user-seen",
                        getUsersInRoom(room, senderId, messageId));
                System.out.println("User " + viewerId + " has seen the message in room: " + room);
            } else {
                System.out.println("Room ID is invalid.");
            }
        } catch (Exception e) {
            System.err.println("Error processing message seen: " + e.getMessage());
            e.printStackTrace();
        }
    };

    public Map<String, String> getUsersInRoom(String roomId, String senderId, String messageId) {
        Map<String, String> users = new HashMap<>();

        if (messageStatus.containsKey(roomId)) {
            Map<String, Boolean> roomMessages = messageStatus.get(roomId);

            // Lọc bỏ người gửi (nếu có trong danh sách)
            for (String userId : roomMessages.keySet()) {
                if (!userId.equals(senderId)) {
                    String name = userService.getUserFullname(Integer.parseInt(userId));
                    users.put(userId, name);
                }
            }
        } else {
            System.out.println("No users have seen messages in room " + roomId);
        }
        return users;
    }
}
