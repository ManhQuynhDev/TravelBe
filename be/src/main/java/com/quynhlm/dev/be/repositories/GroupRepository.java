package com.quynhlm.dev.be.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.quynhlm.dev.be.model.entity.Group;

public interface GroupRepository extends JpaRepository<Group, Integer> {
    @Query(value = "SELECT * FROM m_group WHERE name = :name", nativeQuery = true)
    Group findGroupByName(@Param("name") String name);

    @Query(value = "SELECT * FROM m_group WHERE id = :id", nativeQuery = true)
    Group findGroupById(@Param("id") Integer id);

    Page<Group> findAll(Pageable pageable);
}
