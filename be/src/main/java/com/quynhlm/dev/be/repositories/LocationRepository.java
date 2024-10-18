package com.quynhlm.dev.be.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.quynhlm.dev.be.model.entity.Location;

import java.util.List;


public interface LocationRepository extends JpaRepository<Location, Integer> {
    List<Location> findByName(String name);
}
