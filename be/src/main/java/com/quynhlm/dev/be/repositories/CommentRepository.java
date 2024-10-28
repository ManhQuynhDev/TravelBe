package com.quynhlm.dev.be.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.quynhlm.dev.be.model.entity.Comment;

public interface CommentRepository extends JpaRepository<Comment, Integer> {

    @Query(value = "SELECT * FROM Comment WHERE id = :id", nativeQuery = true)
    Comment findComment(@Param("id") Integer id);

    Page<Comment> findAll(Pageable pageable);
}
