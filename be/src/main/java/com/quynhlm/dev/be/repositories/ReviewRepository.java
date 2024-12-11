package com.quynhlm.dev.be.repositories;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.quynhlm.dev.be.model.entity.Review;

public interface ReviewRepository extends JpaRepository<Review, Integer> {
    Page<Review> findAll(Pageable pageable);

    @Query(value = "SELECT * FROM Review WHERE id = :id", nativeQuery = true)
    Review getAnReview(@Param("id") Integer id);

    @Query(value = """
            select r.id as review_id , r.user_id , r.location_id ,l.address, u.fullname , u.avatar_url ,  r.content , r.media_url , r.star , r.create_time from review r
            inner join User u ON u.id = r.user_id
            INNER JOIN Location l ON l.id = r.location_id
            WHERE r.id = :id;
                            """, nativeQuery = true)
    List<Object[]> getAnReviewDetails(@Param("id") Integer id);

    @Query(value = """
            select r.id as review_id , r.user_id , r.location_id ,l.address, u.fullname , u.avatar_url ,  r.content , r.media_url , r.star , r.create_time from review r
            inner join User u ON u.id = r.user_id
            INNER JOIN Location l ON l.id = r.location_id
            WHERE r.user_id = :user_id;
                                """, nativeQuery = true)
    Page<Object[]> getReviewWithUserId(@Param("user_id") Integer user_id, Pageable pageable);

    @Query(value = """
            select r.id as review_id , r.user_id , r.location_id ,l.address, u.fullname , u.avatar_url ,  r.content , r.media_url , r.star , r.create_time from review r
            inner join User u ON u.id = r.user_id
            INNER JOIN Location l ON l.id = r.location_id
            WHERE r.location_id = :location_id;
                                """, nativeQuery = true)
    Page<Object[]> getReviewWithLocationId(@Param("location_id") Integer location_id, Pageable pageable);
}
