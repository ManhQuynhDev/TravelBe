package com.quynhlm.dev.be.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.quynhlm.dev.be.model.entity.Share;

public interface ShareRepository extends JpaRepository<Share, Integer> {
    @Query(value = "SELECT * FROM Share WHERE id = :id", nativeQuery = true)
    Share getAnShare(@Param("id") int id);
}
