package com.quynhlm.dev.be.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.quynhlm.dev.be.model.entity.Media;
import java.util.List;


public interface MediaRepository extends JpaRepository<Media, Integer> {
    @Query(value = "SELECT * FROM Media WHERE post_id= :post_id", nativeQuery = true)
    Media foundMediaByPostId(@Param("post_id") int post_id);

    @Query(value = "SELECT media_url FROM Media WHERE post_id= :post_id", nativeQuery = true)
    List<String> findMediaByPostId(@Param("post_id") int post_id);

}
