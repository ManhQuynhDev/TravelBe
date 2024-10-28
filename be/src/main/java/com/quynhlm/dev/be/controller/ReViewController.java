package com.quynhlm.dev.be.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.HttpStatus;
import com.quynhlm.dev.be.core.ResponseObject;
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
    public Page<Review> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "2") int size) {
        return reviewService.getListData(page, size);
    }

    @PostMapping("")
    public ResponseEntity<ResponseObject<Void>> insertReview(@RequestPart("review") Review review,
            @RequestPart(value = "file" , required = false) MultipartFile file) throws Exception {
        reviewService.insertReview(review, file);
        ResponseObject<Void> result = new ResponseObject<>();
        result.setMessage("Create a new review successfully");
        return new ResponseEntity<ResponseObject<Void>>(result, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseObject<Review>> findAnReview(@PathVariable Integer id) {
        ResponseObject<Review> result = new ResponseObject<>();
        result.setMessage("Get an review with id " + id + " successfully");
        result.setData(reviewService.findAnReview(id));
        return new ResponseEntity<ResponseObject<Review>>(result, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseObject<Void>> deleteReview(@PathVariable Integer id) {
        ResponseObject<Void> result = new ResponseObject<>();
        reviewService.deleteReview(id);
        result.setMessage("Delete reivew with " + id + "successfully");
        return new ResponseEntity<ResponseObject<Void>>(result, HttpStatus.OK);
    }
}
