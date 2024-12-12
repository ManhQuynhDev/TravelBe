package com.quynhlm.dev.be.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.quynhlm.dev.be.model.entity.HashTag;

public interface HashTagRespository extends JpaRepository<HashTag, Integer> {
    @Query(value = "SELECT hashtag FROM hash_tag WHERE post_id= :post_id", nativeQuery = true)
    List<String> findHashtagByPostId(@Param("post_id") int post_id);
}
