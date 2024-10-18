package com.quynhlm.dev.be.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.quynhlm.dev.be.core.ResponseObject;
import com.quynhlm.dev.be.model.entity.Location;
import com.quynhlm.dev.be.service.LocationService;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
@RequestMapping("/locations")
public class LocationController {

    @Autowired
    private LocationService locationService;

    @PostMapping(path = "")
    public ResponseEntity<ResponseObject<Void>> register(@RequestBody @Valid Location location) {
        locationService.insertLocation(location);
        ResponseObject<Void> result = new ResponseObject<>();
        result.setMessage("Create a location successfully");
        return new ResponseEntity<ResponseObject<Void>>(result, HttpStatus.OK);
    }

}
