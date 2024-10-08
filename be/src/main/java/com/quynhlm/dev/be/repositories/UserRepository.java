package com.quynhlm.dev.be.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.quynhlm.dev.be.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Page<User> findAll(Pageable pageable);

    List<User> findByUsername(String username);

    User findOneByUsername(String username);

    User findOneByEmail(String email);

    List<User> findByEmail(String email);

    Optional<User> findById(Long id);

    List<User> findByPhoneNumber(String phoneNumber);

    // @Modifying
    // @Query(value = "INSERT INTO User (username, password) VALUES (:username, :password)", nativeQuery = true)
    // void register(@Param("username") String username, @Param("phoneNumber") String phoneNumber);
}
