package com.quynhlm.dev.be.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.sql.Timestamp;
import java.util.List;

import com.quynhlm.dev.be.core.exception.PostNotFoundException;
import com.quynhlm.dev.be.core.exception.ReportExistingException;
import com.quynhlm.dev.be.core.exception.ReportNotFoundException;
import com.quynhlm.dev.be.core.exception.UnknownException;
import com.quynhlm.dev.be.core.exception.UserAccountNotFoundException;
import com.quynhlm.dev.be.enums.ResponseReport;
import com.quynhlm.dev.be.model.entity.Post;
import com.quynhlm.dev.be.model.entity.Report;
import com.quynhlm.dev.be.model.entity.User;
import com.quynhlm.dev.be.repositories.PostRepository;
import com.quynhlm.dev.be.repositories.ReportRepository;
import com.quynhlm.dev.be.repositories.UserRepository;

@Service
public class ReportService {
    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private NotificationHelper notificationHelper;

    public void createReport(Report report)
            throws UserAccountNotFoundException, PostNotFoundException, ReportExistingException, UnknownException {

        User user = userRepository.getAnUser(report.getUserId());
        if (user == null) {
            throw new UserAccountNotFoundException(
                    "Found user with " + report.getUserId() + " not found , please try with other id");
        }

        Post foundPost = postRepository.getAnPost(report.getPostId());
        if (foundPost == null) {
            throw new PostNotFoundException(
                    "Found post with " + report.getPostId() + " not found , please try with other id");
        }

        Report foundReport = reportRepository.foundReportExitByUserIdAndPostId(report.getUserId(), report.getPostId());
        if (foundReport != null) {
            throw new ReportExistingException("Report with userId " + report.getUserId() + " and postId "
                    + report.getPostId() + " already exists , please try with other id");
        }

        report.setCreate_time(new Timestamp(System.currentTimeMillis()).toString());
        report.setStatus(ResponseReport.PENDING.name());
        Report saveReport = reportRepository.save(report);
        if (saveReport.getId() == null) {
            throw new UnknownException("Transaction cannot be completed!");
        } else {
            List<User> users = userRepository.findUserWithRole("ROLE_MANAGER", "ROLE_ADMIN");
            for (User u : users) {
                notificationHelper.pushNotification(u.getId(), null,
                        report.getReason(), "Report");
            }
        }
    }

    public void deleteReport(Integer report_id) throws ReportNotFoundException {
        Report report = reportRepository.findReportById(report_id);
        if (report == null) {
            throw new ReportNotFoundException(
                    "Found report with " + report_id + " not found , please try with other id");
        }
        reportRepository.delete(report);
    }

    public void handleReport(Integer userId, Integer report_id, String action) //Valid
            throws UnknownException, UserAccountNotFoundException, ReportNotFoundException {
        Report report = reportRepository.findReportById(report_id);
        if (report == null) {
            throw new ReportNotFoundException(
                    "Found report with " + report_id + " not found , please try with other id");
        }

        User user = userRepository.getAnUser(report.getUserId());
        if (user == null) {
            throw new UserAccountNotFoundException(
                    "Found user with " + report.getUserId() + " not found , please try with other id");
        }

        if (!user.getRoles().contains("ROLE_ADMIN") || !user.getRoles().contains("ROLE_MANAGER")) {
            throw new UnknownException(
                    "User does not have permission to response this report");
        }

        report.setStatus(action);
        report.setResponse_time(new Timestamp(System.currentTimeMillis()).toString());
        Report saveReport = reportRepository.save(report);
        if (saveReport.getId() == null) {
            throw new UnknownException("Transaction cannot be completed!");
        }
    }
}
