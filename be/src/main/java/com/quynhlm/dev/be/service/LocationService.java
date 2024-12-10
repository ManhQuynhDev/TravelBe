package com.quynhlm.dev.be.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
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

    public Location insertLocation(String locationJson) throws LocationExistingException, UnknownException {
        Location location = new Location();
        location.setAddress(locationJson);
        Location saveLocation = locationRepository.save(location);
        if (saveLocation.getId() == null) {
            throw new UnknownException("Transaction cannot complete!");
        }
        return saveLocation;
    }
}
