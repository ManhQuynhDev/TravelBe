package com.quynhlm.dev.be.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.quynhlm.dev.be.model.entity.User;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Page<User> findAll(Pageable pageable);

    List<User> findByUsername(String username);

    User findOneByUsername(String username);

    User findOneByEmail(String email);

    List<User> findByEmail(String email);

    List<User> findByPhoneNumber(String phoneNumber);

    @Query(value = "SELECT * FROM User WHERE id = :id", nativeQuery = true)
    User getAnUser(@Param("id") Long id);
}
