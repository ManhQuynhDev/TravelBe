package com.quynhlm.dev.be.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.quynhlm.dev.be.core.exception.LocationExistingException;
import com.quynhlm.dev.be.core.exception.UnknowException;
import com.quynhlm.dev.be.model.entity.Location;
import com.quynhlm.dev.be.repositories.LocationRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class LocationService {

    @Autowired
    private LocationRepository locationRepository;

    public void insertLocation(Location location) throws LocationExistingException, UnknowException {
        List<Location> foundLocation = locationRepository.findByName(location.getName());
        if (!foundLocation.isEmpty()) {
            throw new LocationExistingException(
                    "Location with " + location.getName() + " already exist !. Please try another!");
        }
        Location saveLocation = locationRepository.save(location);
        if (saveLocation.getId() == null) {
            throw new UnknowException("Transaction cannot complete!");
        }
    }
}
