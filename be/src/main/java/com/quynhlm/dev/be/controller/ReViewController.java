package com.quynhlm.dev.be.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quynhlm.dev.be.core.ResponseObject;
import com.quynhlm.dev.be.model.dto.requestDTO.ReViewRequestDTO;
import com.quynhlm.dev.be.model.dto.requestDTO.ReviewUpdateDTO;
import com.quynhlm.dev.be.model.dto.responseDTO.ReviewResponseDTO;
import com.quynhlm.dev.be.model.entity.Review;
import com.quynhlm.dev.be.service.ReviewService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/reviews")
public class ReViewController {

    @Autowired
    private final ReviewService reviewService;

    @GetMapping("/")
    public Page<Review> getReviews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "2") int size) {
        return reviewService.getListData(page, size);
    }

    @GetMapping("/user_create/{userId}")
    public Page<ReviewResponseDTO> getReviewUserCreate(
            @PathVariable Integer userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "2") int size) {
        return reviewService.getAllReviewUserCreate(userId, page, size);
    }

    @GetMapping("/location/{locationId}")
    public Page<ReviewResponseDTO> getReviewWithLocation(
            @PathVariable Integer locationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "2") int size) {
        return reviewService.getAllReviewWithLocation(locationId, page, size);
    }

    @PostMapping("")
    public ResponseEntity<ResponseObject<ReviewResponseDTO>> insertReview(@RequestPart("review") String reviewJson,
            @RequestPart(value = "file", required = false) MultipartFile file) throws Exception {

        ObjectMapper objectMapper = new ObjectMapper();
        ReViewRequestDTO review = null;
        try {
            review = objectMapper.readValue(reviewJson, ReViewRequestDTO.class);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        ReviewResponseDTO response = reviewService.insertReview(review, file);
        ResponseObject<ReviewResponseDTO> result = new ResponseObject<>();
        result.setMessage("Create a new review successfully");
        result.setStatus(true);
        result.setData(response);
        return new ResponseEntity<ResponseObject<ReviewResponseDTO>>(result, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResponseObject<Void>> updateReview(
            @PathVariable Integer id,
            @RequestPart("review") String reviewJson,
            @RequestPart(value = "file", required = false) MultipartFile file) throws Exception {

        ObjectMapper objectMapper = new ObjectMapper();
        ReviewUpdateDTO review = null;
        try {
            review = objectMapper.readValue(reviewJson, ReviewUpdateDTO.class);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        reviewService.updateReview(id, review, file);
        ResponseObject<Void> result = new ResponseObject<>();
        result.setMessage("Update review successfully");
        result.setStatus(true);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseObject<ReviewResponseDTO>> findAnReview(@PathVariable Integer id) {
        ResponseObject<ReviewResponseDTO> result = new ResponseObject<>();
        result.setMessage("Get an review with id " + id + " successfully");
        result.setStatus(true);
        result.setData(reviewService.findAnReview(id));
        return new ResponseEntity<ResponseObject<ReviewResponseDTO>>(result, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseObject<Void>> deleteReview(@PathVariable Integer id) {
        ResponseObject<Void> result = new ResponseObject<>();
        reviewService.deleteReview(id);
        result.setMessage("Delete reivew with " + id + "successfully");
        result.setStatus(true);
        return new ResponseEntity<ResponseObject<Void>>(result, HttpStatus.OK);
    }
}
