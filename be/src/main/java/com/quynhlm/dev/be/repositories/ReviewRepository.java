package com.quynhlm.dev.be.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.quynhlm.dev.be.model.entity.Review;

public interface ReviewRepository extends JpaRepository<Review , Integer>{
    Page<Review> findAll(Pageable pageable);

    @Query(value = "SELECT * FROM Review WHERE id = :id", nativeQuery = true)
    Review getAnReview(@Param("id") Integer id);

}
