package com.quynhlm.dev.be.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.quynhlm.dev.be.core.ResponseObject;
import com.quynhlm.dev.be.model.entity.Activities;
import com.quynhlm.dev.be.service.ActivitiesService;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PutMapping;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/api/activities")
public class ActivitiesController {
    @Autowired
    private ActivitiesService activitiesService;

    @GetMapping("")
    public Page<Activities> getComments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "2") int size) {
        return activitiesService.getListData(page, size);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseObject<Void>> deleteActivities(@PathVariable Integer id) {
        activitiesService.deleteActivities(id);
        ResponseObject<Void> result = new ResponseObject<>();
        result.setMessage("Delete activity successfully");
        return new ResponseEntity<ResponseObject<Void>>(result, HttpStatus.OK);
    }

    @PostMapping(path = "")
    public ResponseEntity<ResponseObject<Void>> createActivities(@RequestBody Activities activities) {
        activitiesService.createActivities(activities);
        ResponseObject<Void> result = new ResponseObject<>();
        result.setMessage("Create a activities successfully");
        return new ResponseEntity<ResponseObject<Void>>(result, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResponseObject<Void>> updateActivities(@PathVariable int id,
            @RequestBody Activities activities) {
        activitiesService.updateActivities(id, activities);
        ResponseObject<Void> result = new ResponseObject<>();
        result.setMessage("update activities successfully");
        return new ResponseEntity<ResponseObject<Void>>(result, HttpStatus.OK);
    }
}
