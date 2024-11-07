package com.quynhlm.dev.be.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.quynhlm.dev.be.model.entity.Report;

public interface ReportRepository extends JpaRepository<Report, Integer> {
    @Query(value = "SELECT * FROM Report WHERE userId = :userId AND postId = :postId", nativeQuery = true)
    Report foundReportExitByUserIdAndPostId(int userId, int postId);

    @Query(value = "SELECT * FROM Report WHERE id = :id", nativeQuery = true)
    Report findReportById(Integer id);
}
