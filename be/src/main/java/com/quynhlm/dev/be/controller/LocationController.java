package com.quynhlm.dev.be.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.quynhlm.dev.be.core.ResponseObject;
import com.quynhlm.dev.be.model.entity.Location;
import com.quynhlm.dev.be.service.LocationService;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequestMapping("/locations")
public class LocationController {

    @Autowired
    private LocationService locationService;

    @GetMapping("")
    public Page<Location> getAllLoation(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "2") int size) {
        return locationService.getAllLocation(page, size);
    }

    @PostMapping(path = "")
    public ResponseEntity<ResponseObject<Location>> createLocation(@RequestBody Location location) {
        Location locationResponse = locationService.insertLocation(location);
        ResponseObject<Location> result = new ResponseObject<>();
        result.setMessage("Create a location successfully");
        result.setStatus(true);
        result.setData(locationResponse);
        return new ResponseEntity<ResponseObject<Location>>(result, HttpStatus.OK);
    }
}
