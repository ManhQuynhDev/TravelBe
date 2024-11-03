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

    Page<User> findAll(Pageable pageable);

    List<User> findByUsername(String username);

    User findOneByUsername(String username);

    User findOneByEmail(String email);

    List<User> findByEmail(String email);

    List<User> findByPhoneNumber(String phoneNumber);

    @Query(value = "SELECT * FROM User WHERE id = :id", nativeQuery = true)
    User getAnUser(@Param("id") Integer id);

    User findOneById(Integer id);
}
