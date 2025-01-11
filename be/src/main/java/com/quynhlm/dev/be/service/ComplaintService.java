package com.quynhlm.dev.be.service;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;
import java.time.LocalDateTime;

import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.quynhlm.dev.be.core.exception.NotFoundException;
import com.quynhlm.dev.be.core.exception.UnknownException;
import com.quynhlm.dev.be.core.exception.UserAccountNotFoundException;
import com.quynhlm.dev.be.model.dto.requestDTO.ComplaintResquestDTO;
import com.quynhlm.dev.be.model.dto.requestDTO.ComplaintUpdateDTO;
import com.quynhlm.dev.be.model.dto.responseDTO.ComplaintResponseDTO;
import com.quynhlm.dev.be.model.entity.Complaint;
import com.quynhlm.dev.be.model.entity.User;
import com.quynhlm.dev.be.repositories.ComplaintRepository;
import com.quynhlm.dev.be.repositories.UserRepository;

import net.coobird.thumbnailator.Thumbnails;

@Service
public class ComplaintService {

    @Autowired
    private AmazonS3 amazonS3;

    @Value("${aws.s3.bucketName}")
    private String bucketName;

    @Autowired
    private ComplaintRepository complaintRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JavaMailSender mailSender;

    public void createFromComphaint(ComplaintResquestDTO complaintResquestDTO, MultipartFile file)
            throws BadRequestException, UserAccountNotFoundException, UnknownException {

        try {
            Complaint complaint = new Complaint();

            if (complaintResquestDTO.getEmail() == null || complaintResquestDTO.getReason() == null
                    || complaintResquestDTO.getType() == null || complaintResquestDTO.getEmail() == ""
                    || complaintResquestDTO.getType() == "" || complaintResquestDTO.getReason() == "") {
                throw new BadRequestException("Please enter all fields");
            }

            User foundUser = userRepository.findUserByEmail(complaintResquestDTO.getEmail());
            if (foundUser == null) {
                throw new UserAccountNotFoundException(
                        "Found user with id " + complaintResquestDTO.getEmail() + " not found , please try again");
            }

            complaint.setEmail(complaintResquestDTO.getEmail());
            complaint.setType(complaintResquestDTO.getType());
            complaint.setReason(complaintResquestDTO.getReason());
            complaint.setDelflag(0);
            complaint.setCreate_time(LocalDateTime.now().toString());
            complaint.setStatus("PENDING");

            if (file != null && !file.isEmpty()) {
                String mediaUrl = uploadMediaToS3(file);
                complaint.setAttachment(mediaUrl);
            }

            Complaint complaintSave = complaintRepository.save(complaint);
            if (complaintSave.getId() == null) {
                throw new UnknownException("Transaction cannot be completed!");
            }
        } catch (IOException e) {
            throw new UnknownException("File handling error: " + e.getMessage());
        }
    }

    private String uploadMediaToS3(MultipartFile file) throws IOException, UnknownException {
        String fileName = file.getOriginalFilename();
        String contentType = file.getContentType();

        try (InputStream inputStream = file.getInputStream()) {
            if (contentType.startsWith("image/")) {
                BufferedImage originalImage = ImageIO.read(inputStream);

                BufferedImage resizedImage = Thumbnails.of(originalImage)
                        .scale(0.5)
                        .outputQuality(0.1)
                        .asBufferedImage();

                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                ImageIO.write(resizedImage, "jpg", outputStream);
                InputStream resizedInputStream = new ByteArrayInputStream(outputStream.toByteArray());

                ObjectMetadata metadata = new ObjectMetadata();
                metadata.setContentLength(outputStream.size());
                metadata.setContentType(contentType);

                amazonS3.putObject(bucketName, fileName, resizedInputStream, metadata);

            } else if (contentType.startsWith("video/")) {

                ObjectMetadata metadata = new ObjectMetadata();
                metadata.setContentLength(file.getSize());
                metadata.setContentType(contentType);

                amazonS3.putObject(bucketName, fileName, inputStream, metadata);
            }

            String mediaUrl = String.format("https://travle-be.s3.ap-southeast-2.amazonaws.com/%s",
                    fileName);
            return mediaUrl;
        } catch (IOException e) {
            throw new UnknownException("Error uploading file to S3: " + e.getMessage());
        }
    }

    @PostAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public void responseComplaint(Integer id, ComplaintUpdateDTO updateDTO) throws NotFoundException, UnknownException {

        Complaint foundComplaint = complaintRepository.findComplaint(id);
        if (foundComplaint == null) {
            throw new NotFoundException("Found complaint with id " + id + " not found , please try again !");
        }

        foundComplaint.setResponse_message(updateDTO.getResponseMessage());
        foundComplaint.setStatus(updateDTO.getStatus());
        foundComplaint.setResponse_time(LocalDateTime.now().toString());

        sendEmail(
                foundComplaint.getEmail(),
                "Complaint Response",
                "Kính gửi quý khách,\n\n" +
                        "Chúng tôi xin thông báo rằng khiếu nại của bạn đã được xử lý. Kết quả xử lý như sau:\n" +
                        "- Trạng thái: " + updateDTO.getStatus() + "\n" +
                        "- Phản hồi: " + updateDTO.getResponseMessage() + "\n\n" +
                        "Nếu bạn có bất kỳ câu hỏi nào liên quan đến kết quả này, xin vui lòng liên hệ lại với chúng tôi qua email hoặc đường dây hỗ trợ khách hàng.\n\n"
                        +
                        "Trân trọng,\n" +
                        "Đội ngũ hỗ trợ khách hàng");

        Complaint complaintSave = complaintRepository.save(foundComplaint);
        if (complaintSave.getId() == null) {
            throw new UnknownException("Transaction cannot be completed!");
        }
    }

    public Page<Complaint> foundComplaintUserCreate(String email, int page, int size)
            throws UserAccountNotFoundException {
        Pageable pageable = PageRequest.of(page, size);
        User foundUser = userRepository.findUserByEmail(email);
        if (foundUser == null) {
            throw new UserAccountNotFoundException(
                    "Found user with id " + email + " not found , please try again");
        }

        return complaintRepository.foundComplaintUserCreate(email, pageable);
    }

    public Page<ComplaintResponseDTO> getAllComplaint(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        Page<Object[]> results = complaintRepository.getAllComplaint(pageable);
        return results.map((row) -> {
            ComplaintResponseDTO complaintResponseDTO = new ComplaintResponseDTO();
            complaintResponseDTO.setUserId(((Number) row[0]).intValue());
            complaintResponseDTO.setEmail((String) row[1]);
            complaintResponseDTO.setAvatar((String) row[2]);
            complaintResponseDTO.setComplaintId(((Number) row[3]).intValue());
            complaintResponseDTO.setType((String) row[4]);
            complaintResponseDTO.setComplaintReason((String) row[5]);
            complaintResponseDTO.setAttachment((String) row[5]);
            complaintResponseDTO.setStatus((String) row[7]);
            complaintResponseDTO.setResponseTime((String) row[8]);
            complaintResponseDTO.setResponseMessage((String) row[9]);
            complaintResponseDTO.setReport_reason((String) row[10]);
            complaintResponseDTO.setViolation_type((String) row[11]);
            complaintResponseDTO.setLock_date((LocalDateTime) row[12]);

            return complaintResponseDTO;
        });
    }

    private void sendEmail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        mailSender.send(message);
    }
}
