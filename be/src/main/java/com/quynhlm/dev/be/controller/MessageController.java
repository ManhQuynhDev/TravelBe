package com.quynhlm.dev.be.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.quynhlm.dev.be.model.entity.Message;
import com.quynhlm.dev.be.service.MessageService;
import org.springframework.ui.Model;
import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
@RequestMapping(path = "/api/message")
public class MessageController {

    @Autowired
    private MessageService messageService;

    @GetMapping("/")
    public Page<Message> getAllMessage(@PathVariable int group_id, 
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "2") int size) {
        return messageService.getAllListData(group_id, page, size);
    }

    @GetMapping("/index")
    public String getIndexPage(Model model) {
        model.addAttribute("message", "Hello, Thymeleaf!");
        return "index"; 
    }

    // @MessageMapping("/sendMessage")
    // public void sendMessage(@RequestParam(value = "message", required = false)
    // Message message,
    // @RequestParam(value = "file", required = false) MultipartFile file) {

    // if (file != null && !file.isEmpty()) {
    // String fileName = file.getOriginalFilename();
    // long fileSize = file.getSize();
    // String contentType = file.getContentType();

    // if (!isValidFileType(contentType)) {
    // throw new IllegalArgumentException("Invalid file type. Only image and video
    // files are allowed.");
    // }

    // try (InputStream fileInputStream = file.getInputStream()) {
    // ObjectMetadata metadata = new ObjectMetadata();
    // metadata.setContentLength(fileSize);
    // metadata.setContentType(contentType);

    // amazonS3.putObject(bucketName, fileName, fileInputStream, metadata);

    // String fileUrl = amazonS3.getUrl(bucketName, fileName).toString();
    // message.setMediaUrl(fileUrl);
    // } catch (IOException e) {
    // throw new RuntimeException("Error while uploading file to S3", e);
    // }
    // }

    // try {
    // messageService.insertMessage(message);

    // if (message.getGroupId() != null) {
    // messagingTemplate.convertAndSend("/topic/group/" + message.getGroupId(),
    // message);

    // List<Member> approvedMembers =
    // memberRepository.findByGroup_idAndStatus(message.getGroupId(),
    // "APPROVED");
    // for (Member member : approvedMembers) {
    // if (!member.getUserId().equals(message.getUserSendId())) {
    // Group foundGroup = groupRepository.findGroupById(message.getGroupId());
    // Notification notification = notificationService.saveNotification(
    // member.getUserId(),
    // "You have a new message in group " + foundGroup.getName(),
    // message.getContent());
    // messagingTemplate.convertAndSend("/topic/notification/" + member.getUserId(),
    // notification);
    // }
    // }
    // } else {
    // // Gửi tin nhắn một đối một
    // messagingTemplate.convertAndSend("/topic/private/" +
    // message.getUserIdReceived(), message);

    // User foundUser = userRepository.getAnUser(message.getUserSendId());
    // Notification notification = notificationService.saveNotification(
    // message.getUserIdReceived(),
    // "You have a new message from user " + foundUser.getFullname(),
    // message.getContent());
    // messagingTemplate.convertAndSend("/topic/notification/" +
    // message.getUserIdReceived(), notification);
    // }
    // } catch (Exception e) {
    // throw new RuntimeException("An error occurred while processing the message",
    // e);
    // }
    // }

    // private boolean isValidFileType(String contentType) {
    // return contentType.startsWith("image/") || contentType.startsWith("video/");
    // }
}
