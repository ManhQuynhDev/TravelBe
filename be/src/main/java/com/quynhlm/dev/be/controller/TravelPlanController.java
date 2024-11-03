package com.quynhlm.dev.be.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.quynhlm.dev.be.core.ResponseObject;
import com.quynhlm.dev.be.model.entity.Travel_Plan;
import com.quynhlm.dev.be.service.TravelPlanService;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
@RequestMapping("/api/travel-plan")
public class TravelPlanController {

    @Autowired
    private TravelPlanService travelPlanService;

    @PostMapping("")
    public ResponseEntity<ResponseObject<Void>> createTravelPlan(@RequestBody Travel_Plan travel_Plan) {
        ResponseObject<Void> result = new ResponseObject<>();
        travelPlanService.addTravelPlan(travel_Plan);
        result.setMessage("Create a new travel successfully");
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseObject<Void>> deleteTravelPlan(@PathVariable Integer id) {
        travelPlanService.deleteTravelPlan(id);
        ResponseObject<Void> result = new ResponseObject<>();
        result.setMessage("Delete travelPlan successfully");
        return new ResponseEntity<ResponseObject<Void>>(result, HttpStatus.OK);
    }

}
