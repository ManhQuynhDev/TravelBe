package com.quynhlm.dev.be.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.quynhlm.dev.be.model.entity.Complaint;

public interface ComplaintRepository extends JpaRepository<Complaint, Integer> {
    @Query(value = "SELECT * FROM Complaint WHERE id = :id and delflag = 0", nativeQuery = true)
    Complaint findComplaint(@Param("id") Integer id);

    // user-create
    @Query(value = "SELECT * FROM Complaint WHERE email = :email and delflag = 0", nativeQuery = true)
    Page<Complaint> foundComplaintUserCreate(@Param("email") String email, Pageable pageable);

    @Query(value = """
                SELECT
            	u.id ,
                u.email,
                u.avatarUrl,
                c.id as complaint_id,
                c.type,
                c.reason as complaint_reason,
                c.attachment,
                c.status,
                c.response_time,
                c.response_message,
                r.reason as report_reason,
                r.violationType,
                u.lockDate
             FROM Complaint c
            INNER JOIN User u ON u.email = c.email
            INNER JOIN Report r ON r.userId = u.id
            WHERE c.delflag = 0""")
    Page<Object[]> getAllComplaint(Pageable pageable);
}
