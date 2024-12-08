package com.quynhlm.dev.be.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.quynhlm.dev.be.core.ResponseObject;
import com.quynhlm.dev.be.model.dto.responseDTO.PlanResponseDTO;
import com.quynhlm.dev.be.model.entity.Travel_Plan;
import com.quynhlm.dev.be.service.TravelPlanService;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/api/travel-plan")
public class TravelPlanController {

    @Autowired
    private TravelPlanService travelPlanService;

    @GetMapping("")
    public Page<PlanResponseDTO> getAllListGroups(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "2") int size) {
        return travelPlanService.getAllPlans(page, size);
    }

    @GetMapping("/by-group-id/{groupId}")
    public Page<PlanResponseDTO> getAllPlanWithGroupId(
            @PathVariable Integer groupId,
            @RequestParam(defaultValue = "0") int page,      
            @RequestParam(defaultValue = "2") int size) {
        return travelPlanService.getAllPlansWithGroupId(groupId, page, size);
    }

    @PostMapping("")
    public ResponseEntity<ResponseObject<Travel_Plan>> createTravelPlan(@RequestBody Travel_Plan travel_Plan) {
        ResponseObject<Travel_Plan> result = new ResponseObject<>();
        Travel_Plan planResponse = travelPlanService.addTravelPlan(travel_Plan);
        result.setMessage("Create a new travel successfully");
        result.setStatus(true);
        result.setData(planResponse);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseObject<Void>> deleteTravelPlan(@PathVariable Integer id) {
        travelPlanService.deleteTravelPlan(id);
        ResponseObject<Void> result = new ResponseObject<>();
        result.setMessage("Delete travelPlan successfully");
        result.setStatus(true);
        return new ResponseEntity<ResponseObject<Void>>(result, HttpStatus.OK);
    }
}
