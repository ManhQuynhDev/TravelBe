package com.quynhlm.dev.be.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.quynhlm.dev.be.model.entity.Tag;
import java.util.List;

public interface TagRepository extends JpaRepository<Tag, Integer> {

    @Query("SELECT t FROM Tag t WHERE t.id = :id AND t.userId = :userId")
    Tag foundTagWithIdAndUserId(@Param("id") Integer id, @Param("userId") Integer userId);

    @Query("""
                SELECT u.id AS userId, u.fullname AS fullname, u.avatarUrl AS avatarUrl
                FROM Tag t
                INNER JOIN User u ON u.id = t.userId
                WHERE t.postId = :postId
            """)
    List<Object[]> foundUserTagPost(@Param("postId") Integer postId);

    List<Tag> findByPostId(Integer postId);
}
