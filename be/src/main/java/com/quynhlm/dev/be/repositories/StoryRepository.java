package com.quynhlm.dev.be.repositories;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.quynhlm.dev.be.model.entity.Story;

public interface StoryRepository extends JpaRepository<Story, Integer> {
   Page<Story> findAll(Pageable pageable);

   @Query(value = "SELECT * FROM stories WHERE id = :id", nativeQuery = true)
   Story getAnStory(@Param("id") int id);

   @Query(value = """
            SELECT
            u.id AS owner_id,
            s.id AS story_id,
            s.location_id,
            s.content,
            s.status,
            u.fullname AS fullname,
            u.avatar_url as avatar,
            s.music_url,
            s.url AS media_url,
            s.create_time,
            COUNT(DISTINCT r.id) AS reaction_count
         from stories s

         INNER JOIN user u on u.id = s.user_id

         LEFT JOIN
            story_reaction r ON s.id = r.story_id

         GROUP BY
            s.id , u.id , s.location_id;
                """, nativeQuery = true)
   Page<Object[]> fetchStory(Pageable pageable);

   @Modifying
   @Query(value = "UPDATE stories s SET s.del_flag = 1 WHERE s.del_flag = 0 AND s.create_time <= :cutoffTime", nativeQuery = true)
   void updateDelFlag(@Param("cutoffTime") String cutoffTime);

   @Query(value = """
            SELECT
            s.id AS story_id,
            u.id AS owner_id,
            s.location_id,
            s.content,
            s.status,
            u.fullname AS fullname,
            u.avatar_url as avatar,
            s.music_url,
            s.url AS media_url,
            s.create_time,
            COUNT(DISTINCT r.id) AS reaction_count
         FROM stories s
         INNER JOIN user u ON u.id = s.user_id
         LEFT JOIN story_reaction r ON s.id = r.story_id
         WHERE u.id = :userId
         GROUP BY s.id, u.id, s.location_id
                """, nativeQuery = true)
   Page<Object[]> fetchStoryByUserId(@Param("userId") Integer userId, Pageable pageable);

   @Query(value = """
            SELECT
            s.id AS story_id,
            u.id AS owner_id,
            s.location_id,
            s.content,
            s.status,
            u.fullname AS fullname,
            u.avatar_url as avatar,
            s.music_url,
            s.url AS media_url,
            s.create_time
         FROM stories s
         INNER JOIN user u ON u.id = s.user_id
         LEFT JOIN story_reaction r ON s.id = r.story_id
         WHERE u.id = :userId and del_flag = 1
         GROUP BY s.id, u.id, s.location_id
                """, nativeQuery = true)
   List<Object[]> foundStoryByUserId(@Param("userId") Integer userId, Pageable pageable);

   @Query(value = """
             SELECT
             s.id AS story_id,
             u.id AS owner_id,
             s.location_id,
             s.content,
             s.status,
             u.fullname AS fullname,
             u.avatar_url as avatar,
             s.music_url,
             s.url AS media_url,
             s.create_time,
             COUNT(DISTINCT r.id) AS reaction_count
          FROM stories s
          INNER JOIN user u ON u.id = s.user_id
          LEFT JOIN story_reaction r ON s.id = r.story_id
          WHERE u.id IN (:userIds)
          GROUP BY s.id, u.id, s.location_id
         """, nativeQuery = true)
   Page<Object[]> fetchStoriesByUserIds(@Param("userIds") List<Integer> userIds, Pageable pageable);

}
