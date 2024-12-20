package com.quynhlm.dev.be.controller;

import java.util.List;

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
import com.quynhlm.dev.be.repositories.NotificationResponseDTO;
import com.quynhlm.dev.be.service.NotificationService;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    @Autowired
    private NotificationService notificationService;

    @GetMapping("/{userId}")
    public ResponseEntity<ResponseObject<List<NotificationResponseDTO>>> getAllNotificationWithUserId(@PathVariable Integer userId) {
        ResponseObject<List<NotificationResponseDTO>> response = new ResponseObject<>();
        response.setStatus(true);
        response.setMessage("Get all notification Successfully");
        response.setData(notificationService.getAllLotificationWithId(userId));
        return new ResponseEntity<ResponseObject<List<NotificationResponseDTO>>>(response, HttpStatus.OK);
    }

    @GetMapping("/get-an/{id}")
    public ResponseEntity<ResponseObject<NotificationResponseDTO>> getAnNotification(@PathVariable Integer id) {
        ResponseObject<NotificationResponseDTO> response = new ResponseObject<>();
        response.setStatus(true);
        response.setMessage("Get an notification Successfully");
        response.setData(notificationService.getAnLotificationWithId(id));
        return new ResponseEntity<ResponseObject<NotificationResponseDTO>>(response, HttpStatus.OK);
    }
    
    @PostMapping("/")
    public ResponseEntity<ResponseObject<NotificationResponseDTO>> sendNotification(
            @RequestBody Notification notification) {
        ResponseObject<NotificationResponseDTO> response = new ResponseObject<>();
        NotificationResponseDTO result = notificationService.saveNotification(notification);
        response.setStatus(true);
        response.setMessage("Send notification Successfully");
        response.setData(result);
        return new ResponseEntity<ResponseObject<NotificationResponseDTO>>(response, HttpStatus.OK);
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