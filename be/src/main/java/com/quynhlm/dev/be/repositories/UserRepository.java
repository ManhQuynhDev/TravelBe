package com.quynhlm.dev.be.repositories;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.quynhlm.dev.be.model.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    // Page<User> findAll(Pageable pageable);
    @Query(value = "SELECT * FROM User WHERE email = :email AND delflag = 0", nativeQuery = true)
    User getAnUserByEmail(@Param("email") String email);

    @Query(value = """
                SELECT *
                    FROM User
                    WHERE HEX(roles) = 'ACED0005737200136A6176612E7574696C2E41727261794C6973747881D21D99C7619D03000149000473697A657870000000017704000000017400045553455278' AND delflag = 0;
            """, nativeQuery = true)
    List<User> findAllListRolesUser();

    List<User> findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findByPhoneNumber(String phoneNumber);

    @Query(value = "SELECT * FROM User WHERE id = :id AND delflag = 0", nativeQuery = true)
    User getAnUser(@Param("id") Integer id);

    User findOneById(Integer id);

    @Query(value = "SELECT * FROM User WHERE roles = BINARY :param1 OR roles = BINARY :param2 AND delflag = 0", nativeQuery = true)
    List<User> findUserWithRole(@Param("param1") String param1, @Param("param2") String param2);

    @Query(value = "SELECT fullname FROM User WHERE id = :userId AND delflag = 0", nativeQuery = true)
    String findUserFullname(@Param("userId") Integer userId);

    @Query(value = """
                   SELECT u.id,
                   u.fullname,
                   u.email,
                   u.phone_number AS phoneNumber,
                   u.is_locked AS isLocked,
                   u.avatar_url AS avatarUrl,
                   u.create_at
            FROM User u
            WHERE u.delflag = 0;
                                            """, nativeQuery = true)
    Page<Object[]> findAllUser(Pageable pageable);

    @Query(value = """
                     WITH months AS (
                SELECT 1 AS month UNION ALL
                SELECT 2 UNION ALL
                SELECT 3 UNION ALL
                SELECT 4 UNION ALL
                SELECT 5 UNION ALL
                SELECT 6 UNION ALL
                SELECT 7 UNION ALL
                SELECT 8 UNION ALL
                SELECT 9 UNION ALL
                SELECT 10 UNION ALL
                SELECT 11 UNION ALL
                SELECT 12
            )
            SELECT
                m.month AS month_number,
                COALESCE(COUNT(p.id), 0) AS post_count
            FROM months m
            LEFT JOIN (
                SELECT
                    MONTH(create_at) AS month,
                    YEAR(create_at) AS year,
                    id
                FROM user
                WHERE YEAR(create_at) = :year
            ) p ON m.month = p.month
            GROUP BY m.month
            ORDER BY m.month;
                    """, nativeQuery = true)
    List<Object[]> registerInMonth(@Param("year") Integer year);

    @Query(value = """
            SELECT *
            FROM User
            WHERE HEX(roles) <> 'ACED0005737200136A6176612E7574696C2E41727261794C6973747881D21D99C7619D03000149000473697A657870000000017704000000017400045553455278' AND delflag = 0;
             """, nativeQuery = true)
    Page<User> findAllManager(Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.isLocked = :isLocked AND  CAST(u.termDate AS string) LIKE CONCAT(:term_date, '%')")
    List<User> findAllByIsLockedAndLockDateBefore(@Param("isLocked") String isLocked,
            @Param("term_date") String term_date);
}
