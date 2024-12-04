package com.quynhlm.dev.be.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.quynhlm.dev.be.core.exception.GroupNotFoundException;
import com.quynhlm.dev.be.model.dto.requestDTO.MessageRequestDTO;
import com.quynhlm.dev.be.model.dto.requestDTO.MessageSeenDTO;
import com.quynhlm.dev.be.model.dto.responseDTO.UserMessageGroupResponseDTO;
import com.quynhlm.dev.be.model.entity.Group;
import com.quynhlm.dev.be.repositories.GroupRepository;
import com.quynhlm.dev.be.service.MessageGroupService;
import com.quynhlm.dev.be.service.MessageStatusService;
import com.corundumstudio.socketio.SocketIONamespace;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.listener.ConnectListener;
import com.corundumstudio.socketio.listener.DataListener;
import com.corundumstudio.socketio.listener.DisconnectListener;

@Component
public class GroupSocketController {

    @Autowired
    private MessageGroupService messageService;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private MessageStatusService messageStatusService;

    private SocketIONamespace namespace;

    private ConcurrentHashMap<String, Boolean> userTypingMap = new ConcurrentHashMap<>();

    private ConcurrentHashMap<String, String> userSocketMap = new ConcurrentHashMap<>();

    public Map<String, Map<String, Boolean>> messageStatus = new HashMap<>();

    @Autowired
    public GroupSocketController(SocketIOServer server) {
        this.namespace = server.addNamespace("/group");

        this.namespace.addConnectListener(onConnectListener);
        this.namespace.addDisconnectListener(onDisconnectListener);
        this.namespace.addEventListener("onChat", MessageRequestDTO.class, onSendMessage);
        this.namespace.addEventListener("message-seen", MessageSeenDTO.class, onMessageSeen);
        this.namespace.addEventListener("userTyping", String.class, onUserTyping);
        this.namespace.addEventListener("userStoppedTyping", String.class, onUserStoppedTyping);
    }

    public ConnectListener onConnectListener = client -> {
        System.out.println("User " + client.getSessionId() + " connected");

        String userId = client.getHandshakeData().getSingleUrlParam("userId");
        if (userId != null && !userId.isEmpty()) {
            if (userSocketMap.containsKey(userId)) {
                System.out
                        .println("User " + userId + " is already online with socket ID: " + userSocketMap.get(userId));
            } else {
                userSocketMap.put(userId, client.getSessionId().toString());
                System.out.println("User " + userId + " connected with socket ID: " + client.getSessionId());
            }
        } else {
            System.out.println("User connected without a valid userId.");
        }

        // Tiếp tục xử lý phòng chat
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
                // Phát sự kiện tới room
                UserMessageGroupResponseDTO result = messageService.sendMessage(messageRequestDTO);

                this.namespace.getRoomOperations(room).sendEvent("user-chat", result);

                System.out.println("Message sent to room " + room + ": " + message);
            } else {
                System.out.println("Room ID is invalid.");
            }
        } catch (Exception e) {
            System.err.println("Error sending message: " + e.getMessage());
            e.printStackTrace();
        }
    };
    
    public DataListener<MessageSeenDTO> onMessageSeen = (client, messageSeenDTO, ackRequest) -> {
        try {
            String room = messageSeenDTO.getRoomId();
            String viewerId = messageSeenDTO.getViewerId();
            String senderId = messageSeenDTO.getSenderId();
            String messageId = messageSeenDTO.getMessageId();

            System.out.println("MessageId " + messageId);

            if (room != null && !room.isEmpty()) {

                if (viewerId.equals(senderId)) {
                    System.out.println("User " + viewerId + " is the sender, skipping adding to seen list.");
                    return;
                }

                messageStatus.computeIfAbsent(room, k -> new HashMap<>()).put(viewerId, true);

                this.namespace.getRoomOperations(room).sendEvent("user-seen", getUsersInRoom(room, senderId , messageId));
                System.out.println("User " + viewerId + " has seen the message in room: " + room);
            } else {
                System.out.println("Room ID is invalid.");
            }
        } catch (Exception e) {
            System.err.println("Error processing message seen: " + e.getMessage());
            e.printStackTrace();
        }
    };

    // Phương thức trả về danh sách người dùng đã xem tin nhắn trong phòng
    public List<String> getUsersInRoom(String roomId, String senderId , String messageId) {
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

        System.out.println(users);

        for (String userId : users) {
            System.out.println("User seen" + userId);
            messageStatusService.changeStatusMessage(Integer.parseInt(userId), Integer.parseInt(messageId),
            true);
        }
        return users;
    }

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
