package com.quynhlm.dev.be.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.quynhlm.dev.be.core.exception.LocationExistingException;
import com.quynhlm.dev.be.core.exception.UnknownException;
import com.quynhlm.dev.be.model.entity.Location;
import com.quynhlm.dev.be.repositories.LocationRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class LocationService {

    @Autowired
    private LocationRepository locationRepository;

    public Page<Location> getAllLocation (int page , int size) {
        Pageable pageable = PageRequest.of(page, size);
        return locationRepository.findAllLocation(pageable);
    }

    public Location insertLocation(String locationText) throws LocationExistingException, UnknownException {
        Location location = new Location();
        location.setAddress(locationText);
        Location saveLocation = locationRepository.save(location);
        if (saveLocation.getId() == null) {
            throw new UnknownException("Transaction cannot complete!");
        }
        return saveLocation;
    }
}
