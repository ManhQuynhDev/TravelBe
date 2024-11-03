package com.quynhlm.dev.be.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.sql.Timestamp;

import com.quynhlm.dev.be.core.exception.ActivitiesExistingException;
import com.quynhlm.dev.be.core.exception.ActivitiesNotFoundException;
import com.quynhlm.dev.be.core.exception.UnknownException;
import com.quynhlm.dev.be.model.entity.Activities;
import com.quynhlm.dev.be.repositories.ActivitiesRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ActivitiesService {

    @Autowired
    private ActivitiesRepository activitiesRepository;

    // Create activity
    public void createActivities(Activities activities) throws ActivitiesExistingException, UnknownException {
        activities.setCreate_time(new Timestamp(System.currentTimeMillis()).toString());
        Activities foundActivity = activitiesRepository.findByNameAndPlanId(activities.getName(),
                activities.getPlanId());
        if (foundActivity != null) {
            throw new ActivitiesExistingException(
                    "Activity with name " + activities.getName() + " already exist !. Please try another!");
        }
        Activities saveActivity = activitiesRepository.save(activities);
        if (saveActivity.getId() == null) {
            throw new UnknownException("Transaction cannot complete!");
        }
    }

    public void deleteActivities(int id) throws ActivitiesNotFoundException {
        Activities foundActivity = activitiesRepository.findActivities(id);
        if (foundActivity == null) {
            throw new ActivitiesNotFoundException("Activity with id " + id + " not found. Please try another!");
        }
        activitiesRepository.delete(foundActivity);
    }

    public void updateActivities(int id, Activities activities)
            throws ActivitiesNotFoundException, ActivitiesExistingException, UnknownException {

        Activities foundActivity = activitiesRepository.findActivities(id);
        if (foundActivity == null) {
            throw new ActivitiesNotFoundException("Activity with id " + id + " not found. Please try another!");
        }

        Activities isExits = activitiesRepository.findByNameAndPlanId(activities.getName(),
                activities.getPlanId());

        if (isExits != null) {
            throw new ActivitiesExistingException(
                    "Activity with name " + activities.getName() + " already exist !. Please try another!");
        }

        foundActivity.setName(activities.getName());
        foundActivity.setDescription(activities.getDescription());
        foundActivity.setTime(activities.getTime());
        foundActivity.setLocationId(activities.getLocationId());
        foundActivity.setStatus(activities.getStatus());
        foundActivity.setCost(activities.getCost());
        foundActivity.setPlanId(activities.getPlanId());

        Activities saveActivity = activitiesRepository.save(foundActivity);
        if (saveActivity.getId() == null) {
            throw new UnknownException("Transaction cannot be completed!");
        }
    }

    public Page<Activities> getListData(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return activitiesRepository.findAll(pageable);
    }
}
