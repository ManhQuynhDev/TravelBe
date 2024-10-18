package com.quynhlm.dev.be.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.quynhlm.dev.be.model.entity.Media;


public interface MediaRepository extends JpaRepository<Media, Integer> {
    
}
