package com.quynhlm.dev.be.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.quynhlm.dev.be.model.entity.Story;

public interface StoryRepository extends JpaRepository<Story, Integer> {
    @Query(value = "SELECT * FROM Story WHERE id = :id", nativeQuery = true)
    Story getAnStory(@Param("id") int id);
}
