package com.quynhlm.dev.be.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.sql.Timestamp;
import java.util.List;
import java.util.stream.Collectors;

import com.quynhlm.dev.be.core.exception.GroupNotFoundException;
import com.quynhlm.dev.be.core.exception.MemberNotFoundException;
import com.quynhlm.dev.be.core.exception.TravelPlanNotFoundException;
import com.quynhlm.dev.be.core.exception.UnknownException;
import com.quynhlm.dev.be.core.exception.UserAccountNotFoundException;
import com.quynhlm.dev.be.enums.GroupRole;
import com.quynhlm.dev.be.model.dto.responseDTO.MemberPlanResponse;
import com.quynhlm.dev.be.model.dto.responseDTO.PlanResponseDTO;
import com.quynhlm.dev.be.model.entity.Group;
import com.quynhlm.dev.be.model.entity.Location;
import com.quynhlm.dev.be.model.entity.Member;
import com.quynhlm.dev.be.model.entity.MemberPlan;
import com.quynhlm.dev.be.model.entity.Travel_Plan;
import com.quynhlm.dev.be.model.entity.User;
import com.quynhlm.dev.be.repositories.GroupRepository;
import com.quynhlm.dev.be.repositories.LocationRepository;
import com.quynhlm.dev.be.repositories.MemberPlanRepository;
import com.quynhlm.dev.be.repositories.MemberRepository;
import com.quynhlm.dev.be.repositories.TravelPlanRepository;
import com.quynhlm.dev.be.repositories.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class TravelPlanService {
    @Autowired
    private TravelPlanRepository travelPlanRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MemberPlanService memberPlanService;

    @Autowired
    private MemberPlanRepository memberPlanRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private MemberRepository memberRepository;

    public Travel_Plan addTravelPlan(Travel_Plan travelPlan)
            throws UserAccountNotFoundException, MemberNotFoundException, UnknownException, GroupNotFoundException {

        User foundUser = userRepository.getAnUser(travelPlan.getUser_id());
        if (foundUser == null) {
            throw new UserAccountNotFoundException(
                    "Found user with " + travelPlan.getUser_id() + " not found . Please try again !");
        }

        Group foundGroup = groupRepository.findGroupById(travelPlan.getGroup_id());
        if (foundGroup == null) {
            throw new GroupNotFoundException(
                    "Found group with " + travelPlan.getGroup_id() + " not found . Please try again !");
        }

        Location foundLocation = locationRepository.getLocationWithLocation(travelPlan.getLocation());
        if (foundLocation == null) {
            Location location = new Location();
            location.setAddress(travelPlan.getLocation());
            Location saveLocation = locationRepository.save(location);

            travelPlan.setLocation_id(saveLocation.getId());
        } else {
            travelPlan.setLocation_id(foundLocation.getId());
        }

        Member foundUserJoin = memberRepository.findMemberByUserId(foundUser.getId(), foundGroup.getId());
        if (foundUserJoin == null) {
            throw new MemberNotFoundException(
                    "User with id " + foundUser.getId() + " not found in group , please try again");
        }

        travelPlan.setCreate_time(new Timestamp(System.currentTimeMillis()).toString());

        Travel_Plan saveTravelPlan = travelPlanRepository.save(travelPlan);
        if (saveTravelPlan.getId() == null) {
            throw new UnknownException("Transaction cannot complete!");
        }

        MemberPlan member = new MemberPlan();
        member.setUserId(saveTravelPlan.getUser_id());
        member.setPlanId(saveTravelPlan.getId());
        member.setRole(GroupRole.ADMIN.name());
        memberPlanService.setAdminPlan(member);

        return saveTravelPlan;
    }

    public void deleteTravelPlan(int id) throws TravelPlanNotFoundException {
        Travel_Plan foundPlan = travelPlanRepository.getAnTravel_Plan(id);
        if (foundPlan == null) {
            throw new TravelPlanNotFoundException(
                    "Found travel by id " + id + " not found , please try again with other id");
        }

        List<MemberPlan> members = memberPlanRepository.findMemberByPlanId(id);
        log.info("Length member plan : " + members.size());
        if (!members.isEmpty()) {
            for (MemberPlan memberPlan : members) {
                memberPlanService.deleteMemberPlan(memberPlan.getUserId(), memberPlan.getPlanId());
            }
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
            group.setStart_date((String) row[4]);
            group.setEnd_date((String) row[5]);
            group.setDescription((String) row[6]);
            group.setStatus((String) row[7]);
            group.setCreate_time((String) row[8]);
            group.setMember_count(((Number) row[9]).intValue());

            List<Object[]> rawResults = memberPlanRepository.foundMemberJoinPlan(((Number) row[0]).intValue());
            List<MemberPlanResponse> responses = rawResults.stream()
                    .map(r -> new MemberPlanResponse(
                            ((Number) r[0]).intValue(),
                            ((Number) r[1]).intValue(),
                            ((Number) r[2]).intValue(),
                            (String) r[3],
                            (String) r[4],
                            (String) r[5],
                            (String) r[6]))
                    .collect(Collectors.toList());

            group.setUserJoined(responses);
            return group;
        });
    }

    public Page<PlanResponseDTO> getAllPlansWithGroupId(Integer groupId, int page, int size)
            throws GroupNotFoundException {

        Group foundGroup = groupRepository.findGroupById(groupId);
        if (foundGroup == null) {
            throw new GroupNotFoundException(
                    "Found groupId by id " + groupId + " not found , please try again with other id");
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<Object[]> results = travelPlanRepository.fetchPlanWithGroupId(groupId, pageable);

        return results.map(row -> {
            PlanResponseDTO group = new PlanResponseDTO();
            group.setPlanId(((Number) row[0]).intValue());
            group.setAdminId(((Number) row[1]).intValue());
            group.setPlan_name((String) row[2]);
            group.setAdmin_name((String) row[3]);
            group.setStart_date((String) row[4]);
            group.setEnd_date((String) row[5]);
            group.setDescription((String) row[6]);
            group.setStatus((String) row[7]);
            group.setCreate_time((String) row[8]);
            group.setMember_count(((Number) row[9]).intValue());

            List<Object[]> rawResults = memberPlanRepository.foundMemberJoinPlan(((Number) row[0]).intValue());
            List<MemberPlanResponse> responses = rawResults.stream()
                    .map(r -> new MemberPlanResponse(
                            ((Number) r[0]).intValue(),
                            ((Number) r[1]).intValue(),
                            ((Number) r[2]).intValue(),
                            (String) r[3],
                            (String) r[4],
                            (String) r[5],
                            (String) r[6]))
                    .collect(Collectors.toList());

            group.setUserJoined(responses);
            return group;
        });
    }
}
