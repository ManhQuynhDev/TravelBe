package com.quynhlm.dev.be.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import com.quynhlm.dev.be.model.entity.Group;
import com.quynhlm.dev.be.model.entity.Member;
import com.quynhlm.dev.be.model.entity.Message;
import com.quynhlm.dev.be.model.entity.Notification;
import com.quynhlm.dev.be.model.entity.User;
import com.quynhlm.dev.be.repositories.GroupRepository;
import com.quynhlm.dev.be.repositories.MemberRepository;
import com.quynhlm.dev.be.repositories.UserRepository;
import com.quynhlm.dev.be.service.MessageService;
import com.quynhlm.dev.be.service.NotificationService;

import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
public class MessageController {
    @Autowired
    private NotificationService notificationService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private MessageService messageService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private MemberRepository memberRepository;

    @MessageMapping("/sendMessage")
    public void sendMessage(Message message) {

        messageService.insertMessage(message);

        // Send to group
        if (message.getGroupId() != null) {
            messagingTemplate.convertAndSend("/topic/group/" + message.getGroupId(), message);

            List<Member> approvedMembers = memberRepository.findByGroup_idAndStatus(message.getGroupId(), "APPROVED");
            for (Member member : approvedMembers) {
                log.info("Id found member in group: {}", member.getId());
                if (!member.getUserId().equals(message.getUserSendId())) {
                    Group foundGroup = groupRepository.findGroupById(message.getGroupId());
                    Notification notification = notificationService.saveNotification(
                            member.getUserId(), "Bạn có tin nhắn mới trong nhóm " + foundGroup.getName(),
                            message.getContent());
                    messagingTemplate.convertAndSend("/topic/notification/" + member.getUserId(),
                            notification);
                }
            }
        } else {
            // Send one to one
            messagingTemplate.convertAndSend("/topic/private/" + message.getUserIdReceived(), message);

            User foundUser = userRepository.getAnUser(message.getUserSendId());

            Notification notification = notificationService.saveNotification(
                    message.getUserIdReceived(), "Bạn có tin nhắn mới từ người dùng " + foundUser.getFullname(),
                    message.getContent());
            messagingTemplate.convertAndSend("/topic/notification/" + message.getUserIdReceived(), notification);
        }
    }
}
