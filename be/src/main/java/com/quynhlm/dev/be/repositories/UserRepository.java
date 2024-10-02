package com.quynhlm.dev.be.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.quynhlm.dev.be.model.User;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    List<User> findByUsername(String username);

    List<User> findByEmail(String email);

    List<User> findByPhoneNumber(String phoneNumber);

    // @Modifying
    // @Query(value = "INSERT INTO User (username, password) VALUES (:username, :password)", nativeQuery = true)
    // void register(@Param("username") String username, @Param("phoneNumber") String phoneNumber);
}
