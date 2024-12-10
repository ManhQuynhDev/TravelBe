package com.quynhlm.dev.be.service;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.quynhlm.dev.be.core.exception.LocationNotFoundException;
import com.quynhlm.dev.be.core.exception.ReViewNotFoundException;
import com.quynhlm.dev.be.core.exception.UnknownException;
import com.quynhlm.dev.be.core.exception.UserAccountNotFoundException;
import com.quynhlm.dev.be.model.dto.requestDTO.ReViewRequestDTO;
import com.quynhlm.dev.be.model.dto.requestDTO.ReviewUpdateDTO;
import com.quynhlm.dev.be.model.entity.Location;
import com.quynhlm.dev.be.model.entity.Review;
import com.quynhlm.dev.be.model.entity.User;
import com.quynhlm.dev.be.repositories.LocationRepository;
import com.quynhlm.dev.be.repositories.ReviewRepository;
import com.quynhlm.dev.be.repositories.UserRepository;

@Service
public class ReviewService {

    @Autowired
    private ReviewRepository repository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private AmazonS3 amazonS3;

    @Value("${aws.s3.bucketName}")
    private String bucketName;

    public Page<Review> getListData(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return repository.findAll(pageable);
    }

    public Review insertReview(ReViewRequestDTO reViewRequestDTO, MultipartFile file)
            throws UserAccountNotFoundException, LocationNotFoundException, UnknownException {
        try {

            User foundUser = userRepository.getAnUser(reViewRequestDTO.getUser_id());
            if (foundUser == null) {
                throw new UserAccountNotFoundException(
                        "Found user with id " + reViewRequestDTO.getUser_id() + " not found , please try again");
            }

            Location foundLocation = locationRepository.getAnLocation(reViewRequestDTO.getLocation_id());
            if (foundLocation == null) {
                throw new LocationNotFoundException("Found location with id " + reViewRequestDTO.getLocation_id()
                        + " not found , please try again");
            }

            Review review = new Review();
            review.setContent(reViewRequestDTO.getContent());
            review.setUser_id(reViewRequestDTO.getUser_id());
            review.setLocation_id(reViewRequestDTO.getLocation_id());
            review.setStar(reViewRequestDTO.getStar());

            if (file != null && !file.isEmpty()) {
                String fileName = file.getOriginalFilename();
                long fileSize = file.getSize();
                String contentType = file.getContentType();

                try (InputStream inputStream = file.getInputStream()) {

                    ObjectMetadata metadata = new ObjectMetadata();
                    metadata.setContentLength(fileSize);
                    metadata.setContentType(contentType);

                    amazonS3.putObject(bucketName, fileName, inputStream, metadata);

                    review.setMediaUrl(String.format("https://travle-be.s3.ap-southeast-2.amazonaws.com/%s", fileName));
                }
            }

            review.setCreate_time(new Timestamp(System.currentTimeMillis()).toString());
            Review savedReview = repository.save(review);

            if (savedReview.getId() == null) {
                throw new UnknownException("Transaction cannot complete!");
            }
            return review;

        } catch (IOException e) {
            throw new UnknownException("File handling error: " + e.getMessage());
        } catch (Exception e) {
            throw new UnknownException(e.getMessage());
        }
    }

    public Review findAnReview(Integer id) throws ReViewNotFoundException {
        Review review = repository.getAnReview(id);
        if (review == null) {
            throw new ReViewNotFoundException("Id " + id + " not found . Please try another!");
        }
        return review;
    }

    public void deleteReview(Integer id) throws ReViewNotFoundException {
        Review review = repository.getAnReview(id);
        if (review == null) {
            throw new ReViewNotFoundException("Id " + id + " not found . Please try another!");
        }
        repository.delete(review);
    }

    public void updateReview(Integer id, ReviewUpdateDTO newReview, MultipartFile file)
            throws UnknownException, ReViewNotFoundException {
        try {

            Review foundReview = repository.getAnReview(id);
            if (foundReview == null) {
                throw new ReViewNotFoundException("Id " + id + " not found . Please try another!");
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

                    foundReview.setMediaUrl(
                            String.format("https://travle-be.s3.ap-southeast-2.amazonaws.com/%s", fileName));
                }
            }
            foundReview.setContent(newReview.getContent());
            foundReview.setStar(newReview.getStar());
            if (newReview.getLocation_id() == null) {
                foundReview.setLocation_id(newReview.getLocation_id());
            }
            isSuccess(foundReview);
        } catch (IOException e) {
            throw new UnknownException("File handling error: " + e.getMessage());
        } catch (Exception e) {
            throw new UnknownException(e.getMessage());
        }
    }

    public void isSuccess(Review review) {
        Review savedReview = repository.save(review);
        if (savedReview.getId() == null) {
            throw new UnknownException("Transaction cannot complete!");
        }
    }
}
