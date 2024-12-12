package com.quynhlm.dev.be.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.quynhlm.dev.be.model.entity.Location;

import java.util.List;

public interface LocationRepository extends JpaRepository<Location, Integer> {
    List<Location> findByAddress(String address);

    @Query(value = "SELECT * FROM Location WHERE id = :id", nativeQuery = true)
    Location getAnLocation(@Param("id") Integer id);

    @Query(value = """
            SELECT *
            FROM Location""", nativeQuery = true)
    Page<Location> findAllLocation(Pageable pageable);
}
