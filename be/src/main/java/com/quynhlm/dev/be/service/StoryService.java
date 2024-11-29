package com.quynhlm.dev.be.service;

import java.io.IOException;
import java.io.InputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.quynhlm.dev.be.core.exception.StoryNotFoundException;
import com.quynhlm.dev.be.core.exception.UnknownException;
import com.quynhlm.dev.be.core.exception.UserAccountNotFoundException;
import com.quynhlm.dev.be.model.dto.requestDTO.StoryRequestDTO;
import com.quynhlm.dev.be.model.dto.responseDTO.FriendStoryResponseDTO;
import com.quynhlm.dev.be.model.dto.responseDTO.StoryResponseDTO;
import com.quynhlm.dev.be.model.dto.responseDTO.UserTagPostResponse;
import com.quynhlm.dev.be.model.entity.Story;
import com.quynhlm.dev.be.model.entity.User;
import com.quynhlm.dev.be.repositories.FriendShipRepository;
import com.quynhlm.dev.be.repositories.StoryRepository;
import com.quynhlm.dev.be.repositories.UserRepository;

@Service
public class StoryService {

    @Autowired
    private AmazonS3 amazonS3;

    @Autowired
    private FriendShipRepository friendShipRepository;

    @Autowired
    private UserRepository userRepository;

    @Value("${aws.s3.bucketName}")
    private String bucketName;

    @Autowired
    private StoryRepository storyRepository;

    public void deleteStory(int id) throws StoryNotFoundException {
        Story foundStory = storyRepository.getAnStory(id);
        if (foundStory == null) {
            throw new StoryNotFoundException("Story find by " + id + " not found. Please try another!");
        }
        storyRepository.delete(foundStory);
    }

    public Story getAnStory(int id) throws StoryNotFoundException {
        Story foundStory = storyRepository.getAnStory(id);
        if (foundStory == null) {
            throw new StoryNotFoundException("Story find by " + id + " not found. Please try another!");
        }
        return foundStory;
    }

    @Scheduled(fixedRate = 86400000)
    @Transactional
    public void updateDelFlagForStories() {
        Timestamp cutoffTime = new Timestamp(System.currentTimeMillis() - 86400000);
        storyRepository.updateDelFlag(cutoffTime.toString());
    }

    public void insertStory(StoryRequestDTO storyRequestDTO, MultipartFile mediaFile, MultipartFile musicFile)
            throws UnknownException, UserAccountNotFoundException {
        try {

            User foundUser = userRepository.getAnUser(storyRequestDTO.getUser_id());
            if (foundUser == null) {
                throw new UserAccountNotFoundException(
                        "User find by " + storyRequestDTO.getUser_id() + " not found. Please try another!");
            }

            Story story = new Story();
            story.setUser_id(storyRequestDTO.getUser_id());
            story.setContent(storyRequestDTO.getContent());
            story.setHastag(storyRequestDTO.getHastag());
            story.setLocation_id(storyRequestDTO.getLocation_id());
            story.setStatus(storyRequestDTO.getStatus());

            if (mediaFile == null || mediaFile.isEmpty()) {
                throw new UnknownException("No image or video file provided for the story.");
            }

            // Nếu có file nhạc, kiểm tra file nhạc
            if (musicFile != null && !musicFile.isEmpty()) {
                String musicFileName = musicFile.getOriginalFilename();
                long musicFileSize = musicFile.getSize();
                String musicContentType = musicFile.getContentType();

                if (!musicContentType.startsWith("audio/")) {
                    throw new UnknownException("Invalid music file type. Only audio files are allowed.");
                }

                // Upload nhạc lên S3
                try (InputStream musicInputStream = musicFile.getInputStream()) {
                    ObjectMetadata musicMetadata = new ObjectMetadata();
                    musicMetadata.setContentLength(musicFileSize);
                    musicMetadata.setContentType(musicContentType);

                    amazonS3.putObject(bucketName, musicFileName, musicInputStream, musicMetadata);

                    // Lưu đường dẫn file nhạc vào Story (musicId)
                    String musicUrl = String.format("https://travle-be.s3.ap-southeast-2.amazonaws.com/%s",
                            musicFileName);
                    story.setMusic_url(musicUrl);
                }
            }

            // Lấy thông tin file hình ảnh hoặc video
            String imageOrVideoFileName = mediaFile.getOriginalFilename();
            long imageOrVideoFileSize = mediaFile.getSize();
            String imageOrVideoContentType = mediaFile.getContentType();

            // Kiểm tra loại file (chỉ chấp nhận ảnh, video)
            if (!isValidFileType(imageOrVideoContentType)) {
                throw new UnknownException("Invalid file type. Only image or video files are allowed.");
            }

            // Upload file hình ảnh hoặc video lên S3
            try (InputStream imageOrVideoInputStream = mediaFile.getInputStream()) {
                ObjectMetadata imageOrVideoMetadata = new ObjectMetadata();
                imageOrVideoMetadata.setContentLength(imageOrVideoFileSize);
                imageOrVideoMetadata.setContentType(imageOrVideoContentType);

                amazonS3.putObject(bucketName, imageOrVideoFileName, imageOrVideoInputStream, imageOrVideoMetadata);

                // Lưu đường dẫn file hình ảnh hoặc video vào Story (url)
                String imageOrVideoUrl = String.format("https://travle-be.s3.ap-southeast-2.amazonaws.com/%s",
                        imageOrVideoFileName);
                story.setDelFlag(0);
                story.setUrl(imageOrVideoUrl);
                story.setCreate_time(new Timestamp(System.currentTimeMillis()).toString());

                Story savedStory = storyRepository.save(story);
                if (savedStory.getId() == null) {
                    throw new UnknownException("Transaction cannot complete!");
                }
            }
        } catch (IOException e) {
            throw new UnknownException("File handling error: " + e.getMessage());
        } catch (Exception e) {
            throw new UnknownException(e.getMessage());
        }
    }

    // Hàm kiểm tra định dạng file (chỉ chấp nhận ảnh và video)
    private boolean isValidFileType(String contentType) {
        return contentType.startsWith("image/") || contentType.startsWith("video/");
    }

    public Page<Story> getListData(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return storyRepository.findAll(pageable);
    }

    public Page<StoryResponseDTO> getAllStory(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Object[]> results = storyRepository.fetchStory(pageable);

        return results.map(row -> {
            StoryResponseDTO story = new StoryResponseDTO();
            story.setOwnerId(((Number) row[0]).intValue());
            story.setStoryId(((Number) row[1]).intValue());
            story.setLocationId(((Number) row[2]).intValue());
            story.setContent((String) row[3]);
            story.setStatus((String) row[4]);
            story.setFullname((String) row[5]);
            story.setAvatar((String) row[6]);
            story.setMusicUrl((String) row[7]);
            story.setMediaUrl((String) row[8]);
            story.setCreate_time((String) row[9]);
            story.setReaction_count(((Number) row[10]).intValue());
            return story;
        });
    }

    public Page<FriendStoryResponseDTO> fetchFriendStoriesByUserId(Integer userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        List<Object[]> friendShips = friendShipRepository.fetchByUserFriends(userId, "APPROVED");

        List<UserTagPostResponse> responses = friendShips.stream()
                .map(u -> new UserTagPostResponse(
                        ((Number) u[0]).intValue(),
                        (String) u[1],
                        (String) u[2]))
                .collect(Collectors.toList());

        List<FriendStoryResponseDTO> friendStoryResponseList = new ArrayList<>();

        for (UserTagPostResponse user : responses) {

            List<Object[]> results = storyRepository.foundStoryByUserId(user.getUserId(), pageable);

            List<StoryResponseDTO> storys = results.stream()
                    .map(u -> new StoryResponseDTO(
                            ((Number) u[0]).intValue(),
                            ((Number) u[1]).intValue(),
                            ((Number) u[2]).intValue(),
                            (String) u[3],
                            (String) u[4],
                            (String) u[5],
                            (String) u[6],
                            (String) u[7],
                            (String) u[8],
                            (String) u[9],
                            ((Number) u[0]).intValue()))
                    .collect(Collectors.toList());

            FriendStoryResponseDTO friendResult = new FriendStoryResponseDTO();
            friendResult.setUserId(user.getUserId());
            friendResult.setFullname(user.getFullname());
            friendResult.setAvatar(user.getAvatarUrl());
            friendResult.setStorys(storys);

            friendStoryResponseList.add(friendResult);
        }
        return new PageImpl<>(friendStoryResponseList, pageable, friendStoryResponseList.size());
    }
}
