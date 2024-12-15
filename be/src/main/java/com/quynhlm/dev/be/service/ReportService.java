package com.quynhlm.dev.be.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;
import java.util.List;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.quynhlm.dev.be.core.exception.PostNotFoundException;
import com.quynhlm.dev.be.core.exception.ReportExistingException;
import com.quynhlm.dev.be.core.exception.ReportNotFoundException;
import com.quynhlm.dev.be.core.exception.UnknownException;
import com.quynhlm.dev.be.core.exception.UserAccountNotFoundException;
import com.quynhlm.dev.be.enums.ResponseReport;
import com.quynhlm.dev.be.model.dto.requestDTO.ReportRequestDTO;
import com.quynhlm.dev.be.model.dto.responseDTO.ReportResponseDTO;
import com.quynhlm.dev.be.model.entity.Post;
import com.quynhlm.dev.be.model.entity.Report;
import com.quynhlm.dev.be.model.entity.User;
import com.quynhlm.dev.be.repositories.PostRepository;
import com.quynhlm.dev.be.repositories.ReportRepository;
import com.quynhlm.dev.be.repositories.UserRepository;

@Service
public class ReportService {

    @Autowired
    private AmazonS3 amazonS3;

    @Value("${aws.s3.bucketName}")
    private String bucketName;

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    public ReportResponseDTO createReport(ReportRequestDTO reportRequestDTO, MultipartFile file)
            throws UserAccountNotFoundException, PostNotFoundException, ReportExistingException, UnknownException {

        User user = userRepository.getAnUser(reportRequestDTO.getUserId());
        if (user == null) {
            throw new UserAccountNotFoundException(
                    "Found user with " + reportRequestDTO.getUserId() + " not found , please try with other id");
        }

        Post foundPost = postRepository.getAnPost(reportRequestDTO.getPostId());
        if (foundPost == null) {
            throw new PostNotFoundException(
                    "Found post with " + reportRequestDTO.getPostId() + " not found , please try with other id");
        }

        Report foundReport = reportRepository.foundReportExitByUserIdAndPostId(reportRequestDTO.getUserId(),
                reportRequestDTO.getPostId());
        if (foundReport != null) {
            throw new ReportExistingException("Report with userId " + reportRequestDTO.getUserId() + " and postId "
                    + reportRequestDTO.getPostId() + " already exists , please try with other id");
        }

        Report report = new Report();
        report.setPostId(reportRequestDTO.getPostId());
        report.setUserId(reportRequestDTO.getUserId());
        report.setViolationType(reportRequestDTO.getViolationType());
        report.setReason(reportRequestDTO.getReason());

        try {
            if (file != null && !file.isEmpty()) {
                String fileName = file.getOriginalFilename();
                long fileSize = file.getSize();
                String contentType = file.getContentType();

                try (InputStream inputStream = file.getInputStream()) {

                    ObjectMetadata metadata = new ObjectMetadata();
                    metadata.setContentLength(fileSize);
                    metadata.setContentType(contentType);

                    amazonS3.putObject(bucketName, fileName, inputStream, metadata);

                    report.setMediaUrl(String.format("https://travle-be.s3.ap-southeast-2.amazonaws.com/%s", fileName));
                }
            }

            report.setCreate_time(new Timestamp(System.currentTimeMillis()).toString());
            report.setStatus(ResponseReport.PENDING.name());

            Report saveReport = reportRepository.save(report);
            if (saveReport.getId() == null) {
                throw new UnknownException("Transaction cannot be completed!");
            }
            return findReportById(saveReport.getId());
        } catch (IOException e) {
            throw new UnknownException("File handling error: " + e.getMessage());
        } catch (Exception e) {
            throw new UnknownException(e.getMessage());
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

    public void handleReport(Integer userId, Integer report_id, String action)
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

        report.setStatus(action);
        report.setResponse_time(new Timestamp(System.currentTimeMillis()).toString());
        Report saveReport = reportRepository.save(report);
        if (saveReport.getId() == null) {
            throw new UnknownException("Transaction cannot be completed!");
        }
    }

    public ReportResponseDTO findReportById(Integer id) throws ReportNotFoundException {

        List<Object[]> results = reportRepository.getAnReport(id);

        if (results.isEmpty()) {
            throw new ReportNotFoundException(
                    "Id " + id + " not found or invalid data. Please try another!");
        }

        Object[] result = results.get(0);

        ReportResponseDTO report = new ReportResponseDTO();
        report.setId(((Number) result[0]).intValue());
        report.setPostId(((Number) result[1]).intValue());
        report.setOwnerId(((Number) result[2]).intValue());
        report.setFullname((String) result[3]);
        report.setAvatarUrl((String) result[4]);
        report.setContentPost((String) result[5]);
        report.setMediaUrl((String) result[6]);
        report.setMediaType((String) result[7]);
        report.setReason((String) result[8]);
        report.setViolationType((String) result[9]);
        report.setStatus((String) result[10]);
        report.setCreate_time((String) result[11]);
        report.setResponseTime((String) result[12]);

        return report;
    }

    public Page<ReportResponseDTO> getAllReportUserCreate(Integer userId, int page, int size)
            throws UserAccountNotFoundException {

        User user = userRepository.getAnUser(userId);
        if (user == null) {
            throw new UserAccountNotFoundException(
                    "Found user with " + userId + " not found , please try with other id");
        }

        Pageable pageable = PageRequest.of(page, size);

        Page<Object[]> results = reportRepository.getAllReportUserCreate(userId, pageable);

        return results.map(row -> {
            ReportResponseDTO report = new ReportResponseDTO();
            report.setId(((Number) row[0]).intValue());
            report.setPostId(((Number) row[1]).intValue());
            report.setOwnerId(((Number) row[2]).intValue());
            report.setFullname((String) row[3]);
            report.setAvatarUrl((String) row[4]);
            report.setContentPost((String) row[5]);
            report.setMediaUrl((String) row[6]);
            report.setMediaType((String) row[7]);
            report.setReason((String) row[8]);
            report.setViolationType((String) row[9]);
            report.setStatus((String) row[10]);
            report.setCreate_time((String) row[11]);
            report.setResponseTime((String) row[12]);
            return report;
        });
    }

    public Page<ReportResponseDTO> getAllReport(int page, int size) {

        Pageable pageable = PageRequest.of(page, size);

        Page<Object[]> results = reportRepository.getAllReport(pageable);

        return results.map(row -> {
            ReportResponseDTO report = new ReportResponseDTO();
            report.setId(((Number) row[0]).intValue());
            report.setPostId(((Number) row[1]).intValue());
            report.setOwnerId(((Number) row[2]).intValue());
            report.setFullname((String) row[3]);
            report.setAvatarUrl((String) row[4]);
            report.setContentPost((String) row[5]);
            report.setMediaUrl((String) row[6]);
            report.setMediaType((String) row[7]);
            report.setReason((String) row[8]);
            report.setViolationType((String) row[9]);
            report.setStatus((String) row[10]);
            report.setCreate_time((String) row[11]);
            report.setResponseTime((String) row[12]);
            return report;
        });
    }
}
