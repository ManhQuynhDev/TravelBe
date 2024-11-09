package com.quynhlm.dev.be.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.quynhlm.dev.be.model.entity.Tag;

public interface TagRepository extends JpaRepository<Tag, Integer> {

    @Query("SELECT t FROM Tag t WHERE t.id = :id AND t.userId = :userId")
    Tag foundTagWithIdAndUserId(@Param("id") Integer id, @Param("userId") Integer userId);
}
