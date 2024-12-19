package com.quynhlm.dev.be.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.sql.Timestamp;

import com.quynhlm.dev.be.core.exception.ActivitiesExistingException;
import com.quynhlm.dev.be.core.exception.ActivitiesNotFoundException;
import com.quynhlm.dev.be.core.exception.TravelPlanNotFoundException;
import com.quynhlm.dev.be.core.exception.UnknownException;
import com.quynhlm.dev.be.core.exception.UserAccountNotFoundException;
import com.quynhlm.dev.be.model.entity.Activities;
import com.quynhlm.dev.be.model.entity.Travel_Plan;
import com.quynhlm.dev.be.model.entity.User;
import com.quynhlm.dev.be.repositories.ActivitiesRepository;
import com.quynhlm.dev.be.repositories.TravelPlanRepository;
import com.quynhlm.dev.be.repositories.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ActivitiesService {

    @Autowired
    private ActivitiesRepository activitiesRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TravelPlanRepository travelPlanRepository;

    // Create activity
    public Activities createActivities(Activities activities)
            throws ActivitiesExistingException, TravelPlanNotFoundException,
            UserAccountNotFoundException, UnknownException {// Status
        // 1 : 0
        User foundUser = userRepository.getAnUser(activities.getUser_id());
        if (foundUser == null) {
            throw new UserAccountNotFoundException("Found user with id not found please try again");
        }

        Travel_Plan foundPlan = travelPlanRepository.getAnTravel_Plan(activities.getPlanId());
        if (foundPlan == null) {
            throw new TravelPlanNotFoundException("Found group with id not found please try again");
        }

        activities.setCreate_time(new Timestamp(System.currentTimeMillis()).toString());
        Activities saveActivity = activitiesRepository.save(activities);
        if (saveActivity.getId() == null) {
            throw new UnknownException("Transaction cannot complete!");
        }
        return saveActivity;
    }

    public void deleteActivities(int id) throws ActivitiesNotFoundException {
        Activities foundActivity = activitiesRepository.findActivities(id);
        if (foundActivity == null) {
            throw new ActivitiesNotFoundException("Activity with id " + id + " not found. Please try another!");
        }
        activitiesRepository.delete(foundActivity);
    }

    public Activities getAnActivity(int id) throws ActivitiesNotFoundException {
        Activities foundActivity = activitiesRepository.findActivities(id);
        if (foundActivity == null) {
            throw new ActivitiesNotFoundException("Activity with id " + id + " not found. Please try another!");
        }
        return foundActivity;
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

    public Page<Activities> searchActivitiesByName(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return activitiesRepository.searchActivitiesByName(keyword, pageable);
    }

    // Get all data
    public Page<Activities> getListData(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return activitiesRepository.findAll(pageable);
    }

    // Get all data with planid
    public Page<Activities> getActivitiesWithPlanId(int planId, int page, int size) throws TravelPlanNotFoundException {

        Travel_Plan foundPlan = travelPlanRepository.getAnTravel_Plan(planId);
        if (foundPlan == null) {
            throw new TravelPlanNotFoundException("Plan with id  " + planId + " not found please try again");
        }

        Pageable pageable = PageRequest.of(page, size);
        return activitiesRepository.findAllActivitiesWithPlanId(planId, pageable);
    }
}
