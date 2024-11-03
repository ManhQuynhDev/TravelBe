package com.quynhlm.dev.be.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.quynhlm.dev.be.model.entity.Message;
import java.util.List;


@Repository
public interface MessageRepository extends JpaRepository<Message, Integer> {
    List<Message> findByGroupId(Integer groupId);
}