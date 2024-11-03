package com.quynhlm.dev.be.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.sql.Timestamp;

import com.quynhlm.dev.be.core.exception.TravelPlanNotFoundException;
import com.quynhlm.dev.be.core.exception.UnknownException;
import com.quynhlm.dev.be.model.entity.Travel_Plan;
import com.quynhlm.dev.be.repositories.TravelPlanRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TravelPlanService {
    @Autowired
    private TravelPlanRepository travelPlanRepository;

    public void addTravelPlan(Travel_Plan travelPlan) throws UnknownException {

        travelPlan.setCreate_time(new Timestamp(System.currentTimeMillis()).toString());

        Travel_Plan saveTravelPlan = travelPlanRepository.save(travelPlan);
        if (saveTravelPlan.getId() == null) {
            throw new UnknownException("Transaction cannot complete!");
        }
    }

    public void deleteTravelPlan(int id) throws TravelPlanNotFoundException {
        Travel_Plan foundPlan = travelPlanRepository.getAnTravel_Plan(id);
        if (foundPlan == null) {
            throw new TravelPlanNotFoundException(
                    "Found travel by id " + id + " not found , please try again with other id");
        }
        travelPlanRepository.delete(foundPlan);
    }
}
