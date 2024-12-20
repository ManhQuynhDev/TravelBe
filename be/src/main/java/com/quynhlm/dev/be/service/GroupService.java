package com.quynhlm.dev.be.service;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.quynhlm.dev.be.core.exception.GroupExistingException;
import com.quynhlm.dev.be.core.exception.GroupNotFoundException;
import com.quynhlm.dev.be.core.exception.UnknownException;
import com.quynhlm.dev.be.enums.GroupRole;
import com.quynhlm.dev.be.model.dto.requestDTO.GroupRequestDTO;
import com.quynhlm.dev.be.model.dto.requestDTO.SettingsGroupDTO;
import com.quynhlm.dev.be.model.dto.responseDTO.GroupResponseDTO;
import com.quynhlm.dev.be.model.dto.responseDTO.MemberResponseDTO;
import com.quynhlm.dev.be.model.dto.responseDTO.PostStatisticalDTO;
import com.quynhlm.dev.be.model.entity.Group;
import com.quynhlm.dev.be.model.entity.Member;
import com.quynhlm.dev.be.repositories.GroupRepository;
import com.quynhlm.dev.be.repositories.MemberRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class GroupService {

    @Autowired
    private AmazonS3 amazonS3;

    @Value("${aws.s3.bucketName}")
    private String bucketName;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private MemberService memberService;

    @Autowired
    private MemberRepository memberRepository;

    // Create group
    public Group createGroup(GroupRequestDTO groupRequestDTO, MultipartFile file)
            throws GroupExistingException, UnknownException {

        try {
            Group group = new Group();
            group.setUser_id(groupRequestDTO.getUser_id());
            group.setName(groupRequestDTO.getName());
            group.setStatus(groupRequestDTO.getStatus());
            if (groupRequestDTO.getBio() != null) {
                group.setBio(groupRequestDTO.getBio());
            }
            if (file != null && !file.isEmpty()) {
                String fileName = file.getOriginalFilename();
                long fileSize = file.getSize();
                String contentType = file.getContentType();

                try (InputStream inputStream = file.getInputStream()) {

                    ObjectMetadata metadata = new ObjectMetadata();
                    metadata.setContentLength(fileSize);
                    metadata.setContentType(contentType);

                    amazonS3.putObject(bucketName, fileName, inputStream, metadata);

                    group.setCoverPhoto(
                            String.format("https://travle-be.s3.ap-southeast-2.amazonaws.com/%s", fileName));
                }
            }

            Group foundGroup = groupRepository.findGroupByName(group.getName());
            if (foundGroup != null) {
                throw new GroupExistingException("Group name " + group.getName() + " is exits , please try other name");
            }
            group.setCreate_time(new Timestamp(System.currentTimeMillis()).toString());
            Group saveGroup = groupRepository.save(group);
            if (saveGroup.getId() == null) {
                throw new UnknownException("Transaction cannot complete!");
            } else {
                Member member = new Member();
                member.setUserId(saveGroup.getUser_id());
                member.setGroupId(saveGroup.getId());
                member.setRole(GroupRole.ADMIN.name());
                memberService.setAdminGroup(member);
                return saveGroup;
            }
        } catch (IOException e) {
            throw new UnknownException("File handling error: " + e.getMessage());
        } catch (Exception e) {
            throw new UnknownException(e.getMessage());
        }
    }

    public GroupResponseDTO getAnGroupWithId(Integer id) throws GroupNotFoundException {
        List<Object[]> results = groupRepository.findAnGroupById(id);

        if (results.isEmpty()) {
            throw new GroupNotFoundException(
                    "Id " + id + " not found or invalid data. Please try another!");
        }

        Object[] result = results.get(0);

        GroupResponseDTO group = new GroupResponseDTO();
        group.setGroupId(((Number) result[0]).intValue());
        group.setAdminId(((Number) result[1]).intValue());
        group.setGroup_name((String) result[2]);
        group.setAdmin_name((String) result[3]);
        group.setCover_photo((String) result[4]);
        group.setBio((String) result[5]);
        group.setStatus((String) result[6]);
        group.setCreate_time((String) result[7]);
        group.setMember_count(((Number) result[8]).intValue());

        List<Object[]> rawResults = memberRepository.getMemberJoinGroup(((Number) result[0]).intValue());
        List<MemberResponseDTO> responses = rawResults.stream()
                .map(r -> new MemberResponseDTO(
                        ((Number) r[0]).intValue(),
                        ((Number) r[1]).intValue(),
                        ((Number) r[2]).intValue(),
                        (String) r[3],
                        (String) r[4],
                        (String) r[5],
                        (String) r[6],
                        (String) r[7]))
                .collect(Collectors.toList());

        group.setUserJoined(responses);

        return group;
    }

    public Page<GroupResponseDTO> searchGroupsByName(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Object[]> results = groupRepository.searchGroupsByName(keyword, pageable);

        return results.map(row -> {
            GroupResponseDTO group = new GroupResponseDTO();
            group.setGroupId(((Number) row[0]).intValue());
            group.setAdminId(((Number) row[1]).intValue());
            group.setGroup_name((String) row[2]);
            group.setAdmin_name((String) row[3]);
            group.setCover_photo((String) row[4]);
            group.setBio((String) row[5]);
            group.setStatus((String) row[6]);
            group.setCreate_time((String) row[7]);
            group.setMember_count(((Number) row[8]).intValue());

            List<Object[]> rawResults = memberRepository.getMemberJoinGroup(((Number) row[0]).intValue());
            List<MemberResponseDTO> responses = rawResults.stream()
                    .map(r -> new MemberResponseDTO(
                            ((Number) r[0]).intValue(),
                            ((Number) r[1]).intValue(),
                            ((Number) r[2]).intValue(),
                            (String) r[3],
                            (String) r[4],
                            (String) r[5],
                            (String) r[6],
                            (String) r[7]))
                    .collect(Collectors.toList());

            group.setUserJoined(responses);
            return group;
        });
    }

    public void deleteGroup(Integer id) throws GroupNotFoundException {
        Group foundGroup = groupRepository.findGroupById(id);

        if (foundGroup == null) {
            throw new GroupNotFoundException("Group find with " + id + " not found , please try other id");
        }

        List<Member> foundMemberGroup = memberRepository.findByGroupId(foundGroup.getId());

        if (!foundMemberGroup.isEmpty()) {
            for (Member member : foundMemberGroup) {
                memberService.deleteMember(member.getId());
            }
        }
        groupRepository.delete(foundGroup);
    }

    public Page<Group> getListData(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return groupRepository.findAll(pageable);
    }

    public void settingGroup(Integer id, SettingsGroupDTO settingsGroupDTO, MultipartFile file) {
        Group foundGroup = groupRepository.findGroupById(id);
        if (foundGroup == null) {
            throw new GroupNotFoundException("Group with ID " + id + " not found, please try another ID.");
        }

        try {
            if (file != null && !file.isEmpty()) {
                String fileName = file.getOriginalFilename();
                long fileSize = file.getSize();
                String contentType = file.getContentType();

                try (InputStream inputStream = file.getInputStream()) {
                    ObjectMetadata metadata = new ObjectMetadata();
                    metadata.setContentLength(fileSize);
                    metadata.setContentType(contentType);

                    amazonS3.putObject(bucketName, fileName, inputStream, metadata);

                    // Dynamically generate URL
                    String fileUrl = amazonS3.getUrl(bucketName, fileName).toString();
                    foundGroup.setCoverPhoto(fileUrl);
                }
            }

            if (settingsGroupDTO.getName() != null && !settingsGroupDTO.getName().isEmpty()) {
                foundGroup.setName(settingsGroupDTO.getName());
            }
            if (settingsGroupDTO.getBio() != null && !settingsGroupDTO.getBio().isEmpty()) {
                foundGroup.setBio(settingsGroupDTO.getBio());
            }

            if (settingsGroupDTO.getStatus() != null && !settingsGroupDTO.getStatus().isEmpty()) {
                foundGroup.setStatus(settingsGroupDTO.getStatus());
            }

            Group savedGroup = groupRepository.save(foundGroup);
            if (savedGroup.getId() == null) {
                throw new UnknownException("Transaction cannot be completed!");
            }

        } catch (IOException e) {
            throw new UnknownException("File handling error: " + e.getMessage());
        } catch (Exception e) {
            throw new UnknownException(e.getMessage());
        }
    }

    // Get list group
    public Page<GroupResponseDTO> getAllGroup(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Object[]> results = groupRepository.fetchGroup(pageable);

        return results.map(row -> {
            GroupResponseDTO group = new GroupResponseDTO();
            group.setGroupId(((Number) row[0]).intValue());
            group.setAdminId(((Number) row[1]).intValue());
            group.setGroup_name((String) row[2]);
            group.setAdmin_name((String) row[3]);
            group.setCover_photo((String) row[4]);
            group.setBio((String) row[5]);
            group.setStatus((String) row[6]);
            group.setCreate_time((String) row[7]);
            group.setMember_count(((Number) row[8]).intValue());

            List<Object[]> rawResults = memberRepository.getMemberJoinGroup(((Number) row[0]).intValue());
            List<MemberResponseDTO> responses = rawResults.stream()
                    .map(r -> new MemberResponseDTO(
                            ((Number) r[0]).intValue(),
                            ((Number) r[1]).intValue(),
                            ((Number) r[2]).intValue(),
                            (String) r[3],
                            (String) r[4],
                            (String) r[5],
                            (String) r[6],
                            (String) r[7]))
                    .collect(Collectors.toList());

            group.setUserJoined(responses);
            return group;
        });
    }

    public List<GroupResponseDTO> Top10GroupTravel() {
        List<Object[]> results = groupRepository.Top10GroupTravel();

        List<GroupResponseDTO> groupResponseList = new ArrayList<>();

        results.forEach(row -> {
            GroupResponseDTO group = new GroupResponseDTO();

            group.setGroupId(((Number) row[0]).intValue());
            group.setAdminId(((Number) row[1]).intValue());
            group.setGroup_name((String) row[2]);
            group.setAdmin_name((String) row[3]);
            group.setCover_photo((String) row[4]);
            group.setBio((String) row[5]);
            group.setStatus((String) row[6]);
            group.setCreate_time((String) row[7]);
            group.setMember_count(((Number) row[8]).intValue());
            group.setTravel_plan_count(((Number) row[9]).intValue());

            List<Object[]> rawResults = memberRepository.getMemberJoinGroup(((Number) row[0]).intValue());

            List<MemberResponseDTO> memberResponses = rawResults.stream()
                    .map(r -> new MemberResponseDTO(
                            ((Number) r[0]).intValue(),
                            ((Number) r[1]).intValue(),
                            ((Number) r[2]).intValue(),
                            (String) r[3],
                            (String) r[4],
                            (String) r[5],
                            (String) r[6],
                            (String) r[7]))
                    .collect(Collectors.toList());

            group.setUserJoined(memberResponses);

            groupResponseList.add(group);
        });

        return groupResponseList;
    }

    public List<GroupResponseDTO> Top10GroupByMembers() {
        List<Object[]> results = groupRepository.Top10GroupByMembers();

        List<GroupResponseDTO> groupResponseList = new ArrayList<>();

        results.forEach(row -> {
            GroupResponseDTO group = new GroupResponseDTO();

            group.setGroupId(((Number) row[0]).intValue());
            group.setAdminId(((Number) row[1]).intValue());
            group.setGroup_name((String) row[2]);
            group.setAdmin_name((String) row[3]);
            group.setCover_photo((String) row[4]);
            group.setBio((String) row[5]);
            group.setStatus((String) row[6]);
            group.setCreate_time((String) row[7]);
            group.setMember_count(((Number) row[8]).intValue());
            group.setTravel_plan_count(((Number) row[9]).intValue());

            List<Object[]> rawResults = memberRepository.getMemberJoinGroup(((Number) row[0]).intValue());

            List<MemberResponseDTO> memberResponses = rawResults.stream()
                    .map(r -> new MemberResponseDTO(
                            ((Number) r[0]).intValue(),
                            ((Number) r[1]).intValue(),
                            ((Number) r[2]).intValue(),
                            (String) r[3],
                            (String) r[4],
                            (String) r[5],
                            (String) r[6],
                            (String) r[7]))
                    .collect(Collectors.toList());

            group.setUserJoined(memberResponses);

            groupResponseList.add(group);
        });

        return groupResponseList;
    }

    public List<PostStatisticalDTO> getPostCreateCount(int year) {
        List<Object[]> results = groupRepository.groupCreateInMonth(year);

        List<PostStatisticalDTO> postStatisticalDTOs = new ArrayList<>();

        for (Object[] row : results) {
            PostStatisticalDTO dto = new PostStatisticalDTO();
            dto.setMonth(((Number) row[0]).intValue());
            dto.setPost_count(((Number) row[1]).intValue());
            postStatisticalDTOs.add(dto);
        }
        return postStatisticalDTOs;
    }
}
