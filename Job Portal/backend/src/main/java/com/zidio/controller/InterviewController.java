package com.zidio.controller;

import com.zidio.entity.Interview;
import com.zidio.entity.User;
import com.zidio.repository.InterviewRepository;
import com.zidio.repository.UserRepository;
import com.zidio.service.EmailService;
import com.zidio.util.SecurityUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/interviews")
public class InterviewController {

    private final InterviewRepository interviewRepo;
    private final UserRepository userRepo;
    private final EmailService emailService;

    public InterviewController(InterviewRepository interviewRepo, UserRepository userRepo, EmailService emailService) {
        this.interviewRepo = interviewRepo;
        this.userRepo = userRepo;
        this.emailService = emailService;
    }

    @GetMapping
    public List<Interview> getAll() {
        return interviewRepo.findAll();
    }

    @PostMapping("/schedule")
    public ResponseEntity<?> schedule(@RequestBody Interview interview) {
        User current = SecurityUtils.getCurrentUser();
        if (current == null) return ResponseEntity.status(401).body("Unauthorized");

        interviewRepo.save(interview);

        // Send email notification to candidate
        userRepo.findById(interview.getCandidateId()).ifPresent(candidate -> {
            String subject = "Interview Scheduled for " + interview.getJobId();
            String body = "Hi " + candidate.getFullName() + ",\n\n" +
                    "Your interview has been scheduled.\n" +
                    "Date & Time: " + interview.getScheduledAt() + "\n" +
                    "Mode: " + interview.getMode() + "\n" +
                    "Location/Link: " + interview.getLocation() + "\n\n" +
                    "Regards,\nZidio Connect";
            emailService.sendEmail(candidate.getEmail(), subject, body);
        });

        // Send confirmation to recruiter too
        userRepo.findById(interview.getRecruiterId()).ifPresent(recruiter -> {
            String subject = "Interview Scheduled with " + interview.getCandidateId();
            String body = "Hi " + recruiter.getFullName() + ",\n\n" +
                    "You have scheduled an interview.\n" +
                    "Date & Time: " + interview.getScheduledAt() + "\n" +
                    "Candidate ID: " + interview.getCandidateId() + "\n" +
                    "Job ID: " + interview.getJobId() + "\n\n" +
                    "Zidio Connect System";
            emailService.sendEmail(recruiter.getEmail(), subject, body);
        });

        return ResponseEntity.ok("Interview scheduled & emails sent!");
    }
}

