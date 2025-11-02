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
import java.util.Optional;

@RestController
@RequestMapping("/api/interviews")
public class InterviewController {
    private final InterviewRepository interviewRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    public InterviewController(InterviewRepository interviewRepository, UserRepository userRepository, EmailService emailService) {
        this.interviewRepository = interviewRepository; this.userRepository = userRepository; this.emailService = emailService;
    }

    @PostMapping("/schedule")
    public ResponseEntity<?> schedule(@RequestBody Interview request) {
        User me = SecurityUtils.getCurrentUser();
        if (me == null) return ResponseEntity.status(401).body("Unauthorized");
        if (!"RECRUITER".equalsIgnoreCase(me.getRole()) && !"ADMIN".equalsIgnoreCase(me.getRole()))
            return ResponseEntity.status(403).body("Forbidden");
        request.setRecruiterId(me.getId());
        Interview saved = interviewRepository.save(request);
        Optional<User> candidate = userRepository.findById(saved.getCandidateId());
        Optional<User> recruiter = userRepository.findById(saved.getRecruiterId());
        String subject = "Interview Scheduled: " + (saved.getScheduledAt()!=null ? saved.getScheduledAt().toString() : "");
        String body = "Interview for job id " + saved.getJobId() + " scheduled at " + saved.getScheduledAt() + ". Mode: " + saved.getMode() + ". Link/Location: " + saved.getLocation();
        candidate.ifPresent(c -> { if (c.getEmail()!=null) emailService.sendSimpleEmail(c.getEmail(), subject, "Hello " + c.getFullName() + ",\n\n" + body); });
        recruiter.ifPresent(r -> { if (r.getEmail()!=null) emailService.sendSimpleEmail(r.getEmail(), subject, "Hello " + r.getFullName() + ",\n\n" + body); });
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/my")
    public ResponseEntity<?> myInterviews() {
        User me = SecurityUtils.getCurrentUser();
        if (me == null) return ResponseEntity.status(401).body("Unauthorized");
        List<Interview> interviews;
        if ("RECRUITER".equalsIgnoreCase(me.getRole())) interviews = interviewRepository.findByRecruiterId(me.getId());
        else interviews = interviewRepository.findByCandidateId(me.getId());
        return ResponseEntity.ok(interviews);
    }
}
