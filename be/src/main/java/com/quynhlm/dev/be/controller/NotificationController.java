package com.quynhlm.dev.be.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.quynhlm.dev.be.core.ResponseObject;
import com.quynhlm.dev.be.model.entity.Notification;
import com.quynhlm.dev.be.service.NotificationService;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    @Autowired
    private NotificationService notificationService;

    @PostMapping("/")
    public ResponseEntity<ResponseObject<Notification>> sendNotification(
            @RequestBody Notification notification) {
        ResponseObject<Notification> response = new ResponseObject<>();

        Notification result = notificationService.saveNotification(notification);
        response.setStatus(true);
        response.setMessage("Send notification Successfully");
        response.setData(result);
        return new ResponseEntity<ResponseObject<Notification>>(response, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseObject<Void>> deleteNotification(
            @PathVariable Integer id) {
        ResponseObject<Void> response = new ResponseObject<>();
        notificationService.deleteNotification(id);
        response.setStatus(true);
        response.setMessage("Delete notification sucessfully");
        return new ResponseEntity<ResponseObject<Void>>(response, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResponseObject<Void>> updateStatusNotification(@PathVariable Integer id) {
        ResponseObject<Void> response = new ResponseObject<>();
        notificationService.changeStatus(id);
        response.setStatus(true);
        response.setMessage("Update Status Notification Successfully");
        return new ResponseEntity<ResponseObject<Void>>(response, HttpStatus.OK);
    }

}