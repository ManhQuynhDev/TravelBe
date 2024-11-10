package com.quynhlm.dev.be.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.sql.Timestamp;

import com.quynhlm.dev.be.core.exception.TravelPlanNotFoundException;
import com.quynhlm.dev.be.core.exception.UnknownException;
import com.quynhlm.dev.be.core.exception.UserAccountNotFoundException;
import com.quynhlm.dev.be.enums.GroupRole;
import com.quynhlm.dev.be.model.dto.responseDTO.PlanResponseDTO;
import com.quynhlm.dev.be.model.entity.MemberPlan;
import com.quynhlm.dev.be.model.entity.Travel_Plan;
import com.quynhlm.dev.be.model.entity.User;
import com.quynhlm.dev.be.repositories.TravelPlanRepository;
import com.quynhlm.dev.be.repositories.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TravelPlanService {
    @Autowired
    private TravelPlanRepository travelPlanRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MemberPlanService memberPlanService;

    public void addTravelPlan(Travel_Plan travelPlan) throws UserAccountNotFoundException, UnknownException {

        User foundUser = userRepository.getAnUser(travelPlan.getUser_id());
        if (foundUser == null) {
            throw new UserAccountNotFoundException(
                    "Found user with " + travelPlan.getUser_id() + " not found . Please try again !");
        }

        travelPlan.setCreate_time(new Timestamp(System.currentTimeMillis()).toString());

        Travel_Plan saveTravelPlan = travelPlanRepository.save(travelPlan);
        if (saveTravelPlan.getId() == null) {
            throw new UnknownException("Transaction cannot complete!");
        } else {
            MemberPlan member = new MemberPlan();
            member.setUserId(saveTravelPlan.getUser_id());
            member.setPlanId(saveTravelPlan.getId());
            member.setRole(GroupRole.ADMIN.name());
            memberPlanService.setAdminPlan(member);
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

    public Page<PlanResponseDTO> getAllPlans(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Object[]> results = travelPlanRepository.fetchPlans(pageable);

        return results.map(row -> {
            PlanResponseDTO group = new PlanResponseDTO();
            group.setPlanId(((Number) row[0]).intValue());
            group.setAdminId(((Number) row[1]).intValue());
            group.setPlan_name((String) row[2]);
            group.setAdmin_name((String) row[3]);
            group.setDescription((String) row[4]);
            group.setStatus((String) row[5]);
            group.setCreate_time((String) row[6]);
            group.setMember_count(((Number) row[7]).intValue());
            return group;
        });
    }
}
