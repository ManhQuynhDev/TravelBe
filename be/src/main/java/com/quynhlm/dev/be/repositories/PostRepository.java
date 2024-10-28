package com.quynhlm.dev.be.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.quynhlm.dev.be.model.entity.Post;


public interface PostRepository extends JpaRepository<Post, Integer> {
    @Query(value = "SELECT p.id, p.content, p.status, p.location_id, p.hastag, m.media_url, m.type " +
            "FROM Post p " +
            "INNER JOIN Media m ON p.id = m.post_id", countQuery = "SELECT COUNT(*) FROM Post p INNER JOIN Media m ON p.id = m.post_id", nativeQuery = true)
    Page<Object[]> fetchPostWithMedia(Pageable pageable);

    @Query(value = "SELECT * FROM Post WHERE id= :id", nativeQuery = true)
    Post getAnPost(@Param("id") int id);
}
