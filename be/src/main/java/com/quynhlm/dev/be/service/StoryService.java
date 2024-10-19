package com.quynhlm.dev.be.service;

import java.io.IOException;
import java.io.InputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.sql.Timestamp;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.quynhlm.dev.be.core.exception.StoryNotFoundException;
import com.quynhlm.dev.be.core.exception.UnknowException;
import com.quynhlm.dev.be.model.entity.Story;
import com.quynhlm.dev.be.repositories.StoryRepository;

@Service
public class StoryService {

    @Autowired
    private AmazonS3 amazonS3;

    @Value("${aws.s3.bucketName}")
    private String bucketName;

    @Autowired
    private StoryRepository storyRepository;

    public void deleteStory(int id) throws StoryNotFoundException {
        Story foundStory = storyRepository.getAnStory(id);
        if (foundStory == null) {
            throw new StoryNotFoundException("User find by " + id + " not found. Please try another!");
        }
        storyRepository.delete(foundStory);
    }

    public void insertStory(Story story, MultipartFile mediaFile, MultipartFile musicFile)
            throws UnknowException {
        try {
            if (mediaFile == null || mediaFile.isEmpty()) {
                throw new UnknowException("No image or video file provided for the story.");
            }

            // Nếu có file nhạc, kiểm tra file nhạc
            if (musicFile != null && !musicFile.isEmpty()) {
                String musicFileName = musicFile.getOriginalFilename();
                long musicFileSize = musicFile.getSize();
                String musicContentType = musicFile.getContentType();

                if (!musicContentType.startsWith("audio/")) {
                    throw new UnknowException("Invalid music file type. Only audio files are allowed.");
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
                throw new UnknowException("Invalid file type. Only image or video files are allowed.");
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
                story.setUrl(imageOrVideoUrl);
                story.setCreate_time(new Timestamp(System.currentTimeMillis()).toString());

                Story savedStory = storyRepository.save(story);
                if (savedStory.getId() == null) {
                    throw new UnknowException("Transaction cannot complete!");
                }
            }
        } catch (IOException e) {
            throw new UnknowException("File handling error: " + e.getMessage());
        } catch (Exception e) {
            throw new UnknowException(e.getMessage());
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

}
