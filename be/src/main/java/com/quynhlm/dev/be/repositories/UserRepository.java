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

    User findOneByEmail(String email);

    List<User> findByEmail(String email);

    List<User> findByPhoneNumber(String phoneNumber);

    @Query(value = "SELECT * FROM User WHERE id = :id", nativeQuery = true)
    User getAnUser(@Param("id") Integer id);

    User findOneById(Integer id);

    @Query(value = "SELECT * FROM User WHERE roles = BINARY :param1 OR roles = BINARY :param2", nativeQuery = true)
    List<User> findUserWithRole(@Param("param1") String param1, @Param("param2") String param2);

    @Query(value = """
                   SELECT u.id,
                   u.fullname,
                   u.email,
                   u.phone_number AS phoneNumber,
                   u.is_locked AS isLocked,
                   u.avatar_url AS avatarUrl,
                   u.create_at
            FROM User u;
                                            """, nativeQuery = true)
    Page<Object[]> findAllUser(Pageable pageable);
}
