package com.zidio.controller;

import com.zidio.entity.Interview;
import com.zidio.entity.User;
import com.zidio.repository.InterviewRepository;
import com.zidio.repository.UserRepository;
import com.zidio.service.EmailService;
import com.zidio.util.EmailTemplateUtil;
import com.zidio.util.AdminEmailTemplateUtil;
import com.zidio.util.SecurityUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

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

    // Get all interviews
    @GetMapping
    public List<Interview> getAll() {
        return interviewRepo.findAll();
    }

    // Schedule new interview
    @PostMapping("/schedule")
    public ResponseEntity<?> schedule(@RequestBody Interview interview) {
        User current = SecurityUtils.getCurrentUser();
        if (current == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

        interviewRepo.save(interview);
        sendEmailsForAction(interview, "scheduled");
        return ResponseEntity.ok("Interview scheduled & all HTML emails sent!");
    }

    // Reschedule interview
    @PutMapping("/reschedule/{id}")
    public ResponseEntity<?> reschedule(@PathVariable Long id, @RequestBody Interview updated) {
        Optional<Interview> existing = interviewRepo.findById(id);
        if (existing.isEmpty()) {
            return ResponseEntity.status(404).body("Interview not found");
        }

        Interview interview = existing.get();
        interview.setScheduledAt(updated.getScheduledAt());
        interview.setMode(updated.getMode());
        interview.setLocation(updated.getLocation());
        interviewRepo.save(interview);

        sendEmailsForAction(interview, "rescheduled");
        return ResponseEntity.ok("Interview rescheduled & notifications sent!");
    }

    // Cancel interview
    @DeleteMapping("/cancel/{id}")
    public ResponseEntity<?> cancel(@PathVariable Long id) {
        Optional<Interview> existing = interviewRepo.findById(id);
        if (existing.isEmpty()) {
            return ResponseEntity.status(404).body("Interview not found");
        }

        Interview interview = existing.get();
        interviewRepo.delete(interview);

        sendEmailsForAction(interview, "cancelled");
        return ResponseEntity.ok("Interview cancelled & notifications sent!");
    }

    // Centralized email sender for all actions
    private void sendEmailsForAction(Interview interview, String action) {
        String actionWord = action.substring(0, 1).toUpperCase() + action.substring(1);

        // Candidate Email
        userRepo.findById(interview.getCandidateId()).ifPresent(candidate -> {
            try {
                String subject = "Your Interview has been " + actionWord + " – Zidio Connect";
                String html = switch (action) {
                    case "rescheduled" -> """
                        <html><body style='font-family:Arial;background:#f8fafc;padding:20px;'>
                          <h2 style='color:#0f172a;'>Interview Rescheduled</h2>
                          <p>Hi <b>%s</b>,</p>
                          <p>Your interview has been <b>rescheduled</b> with updated details:</p>
                          <ul>
                            <li><b>Date & Time:</b> %s</li>
                            <li><b>Mode:</b> %s</li>
                            <li><b>Link:</b> <a href='%s'>%s</a></li>
                          </ul>
                          <p>We apologize for any inconvenience and look forward to your participation.</p>
                          <p>– Zidio Connect Team</p>
                        </body></html>
                    """.formatted(candidate.getFullName(), interview.getScheduledAt(),
                            interview.getMode(), interview.getLocation(), interview.getLocation());
                    case "cancelled" -> """
                        <html><body style='font-family:Arial;background:#f8fafc;padding:20px;'>
                          <h2 style='color:#b91c1c;'>Interview Cancelled</h2>
                          <p>Hi <b>%s</b>,</p>
                          <p>We regret to inform you that your scheduled interview has been <b>cancelled</b>.</p>
                          <p>If required, the recruiter may contact you to reschedule at a later date.</p>
                          <p>– Zidio Connect Team</p>
                        </body></html>
                    """.formatted(candidate.getFullName());
                    default -> EmailTemplateUtil.interviewScheduledTemplate(candidate, interview);
                };
                emailService.sendHtmlEmail(candidate.getEmail(), subject, html);
            } catch (Exception e) {
                System.err.println("❌ Candidate email (" + action + ") failed: " + e.getMessage());
            }
        });

        // Recruiter Email
        userRepo.findById(interview.getRecruiterId()).ifPresent(recruiter -> {
            try {
                String subject = "Interview " + actionWord + " Confirmation – Zidio Connect";
                String html = """
                    <html><body style='font-family:Arial;background:#f8fafc;padding:20px;'>
                      <h2 style='color:#0f172a;'>Interview %s</h2>
                      <p>Hi <b>%s</b>,</p>
                      <p>The interview with candidate ID <b>%d</b> has been <b>%s</b>.</p>
                      <p>Date & Time: %s<br/>
                         Mode: %s<br/>
                         Link: <a href='%s'>%s</a></p>
                      <p>– Zidio Connect System</p>
                    </body></html>
                """.formatted(actionWord, recruiter.getFullName(),
                        interview.getCandidateId(), action,
                        interview.getScheduledAt(), interview.getMode(),
                        interview.getLocation(), interview.getLocation());
                emailService.sendHtmlEmail(recruiter.getEmail(), subject, html);
            } catch (Exception e) {
                System.err.println("❌ Recruiter email (" + action + ") failed: " + e.getMessage());
            }
        });

        // Admin Notification
        userRepo.findAll().stream()
                .filter(user -> "ADMIN".equalsIgnoreCase(user.getRole()))
                .findFirst()
                .ifPresent(admin -> {
                    try {
                        userRepo.findById(interview.getRecruiterId()).ifPresent(recruiter -> {
                            userRepo.findById(interview.getCandidateId()).ifPresent(candidate -> {
                                String subject = "Admin Alert: Interview " + actionWord + " – Zidio Connect";
                                String html = """
                                    <html><body style='font-family:Arial;background:#f8fafc;padding:20px;'>
                                      <h2 style='color:#0f172a;'>Admin Notification</h2>
                                      <p>A scheduled interview has been <b>%s</b> on the platform.</p>
                                      <ul>
                                        <li><b>Recruiter:</b> %s (%s)</li>
                                        <li><b>Candidate:</b> %s (%s)</li>
                                        <li><b>Job ID:</b> %d</li>
                                        <li><b>Status:</b> %s</li>
                                      </ul>
                                      <p>– Zidio Connect System</p>
                                    </body></html>
                                """.formatted(actionWord,
                                        recruiter.getFullName(), recruiter.getEmail(),
                                        candidate.getFullName(), candidate.getEmail(),
                                        interview.getJobId(), actionWord.toUpperCase());
                                emailService.sendHtmlEmail(admin.getEmail(), subject, html);
                            });
                        });
                    } catch (Exception e) {
                        System.err.println("❌ Admin email (" + action + ") failed: " + e.getMessage());
                    }
                });
    }
}




