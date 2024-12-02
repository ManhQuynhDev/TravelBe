package com.quynhlm.dev.be.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.corundumstudio.socketio.SocketIONamespace;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.listener.ConnectListener;
import com.corundumstudio.socketio.listener.DataListener;
import com.corundumstudio.socketio.listener.DisconnectListener;
import com.quynhlm.dev.be.model.dto.requestDTO.MessageSeenDTO;
import com.quynhlm.dev.be.model.dto.responseDTO.MessageDTO;
import com.quynhlm.dev.be.model.dto.responseDTO.UserMessageResponseDTO;
import com.quynhlm.dev.be.model.entity.Message;
import com.quynhlm.dev.be.service.MessageService;

@Component
public class MessageSocketController {

    @Autowired
    private MessageService messageService;

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
            userTypingMap.put(user_id, true);
            namespace.getRoomOperations(room).sendEvent("typingUsers", userTypingMap);
        }
    };

    public DataListener<String> onUserStoppedTyping = (client, user_id, ackRequest) -> {
        String senderId = client.getHandshakeData().getSingleUrlParam("sender");
        String receiverId = client.getHandshakeData().getSingleUrlParam("receiverId");

        String room = generateRoom(senderId, receiverId);

        if (room != null && !room.isEmpty()) {
            userTypingMap.remove(user_id);
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

            UserMessageResponseDTO result = messageService.sendMessage(saveMessage);

            this.namespace.getRoomOperations(room).sendEvent("user-chat", result);
        } catch (Exception e) {
            System.err.println("Error in onSendMessage event: " + e.getMessage());
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

    public List<String> getUsersInRoom(String roomId, String senderId, String messageId) {
        List<String> users = new ArrayList<>();

        if (messageStatus.containsKey(roomId)) {
            Map<String, Boolean> roomMessages = messageStatus.get(roomId);

            // Lọc bỏ người gửi (nếu có trong danh sách)
            for (String userId : roomMessages.keySet()) {
                if (!userId.equals(senderId)) {
                    users.add(userId);
                }
            }
        } else {
            System.out.println("No users have seen messages in room " + roomId);
        }
        for (String userId : users) {
            System.out.println("User seen" + userId);
            // messageService.changeStatusMessage(Integer.parseInt(messageId), true);
        }
        return users;
    }
}
