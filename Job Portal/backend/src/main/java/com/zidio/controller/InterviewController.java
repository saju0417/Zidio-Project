package com.zidio.controller;

import com.zidio.entity.Interview;
import com.zidio.entity.User;
import com.zidio.repository.InterviewRepository;
import com.zidio.repository.UserRepository;
import com.zidio.service.EmailService;
import com.zidio.util.EmailTemplateUtil;
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

    // Get all scheduled interviews
    @GetMapping
    public List<Interview> getAll() {
        return interviewRepo.findAll();
    }

    // Schedule a new interview
    @PostMapping("/schedule")
    public ResponseEntity<?> schedule(@RequestBody Interview interview) {
        User current = SecurityUtils.getCurrentUser();
        if (current == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

        // Save the interview in DB
        interviewRepo.save(interview);

        // ✅ Send styled HTML email to candidate
        userRepo.findById(interview.getCandidateId()).ifPresent(candidate -> {
            try {
                String subject = "Your Interview is Scheduled – Zidio Connect";
                String html = EmailTemplateUtil.interviewScheduledTemplate(candidate, interview);
                emailService.sendHtmlEmail(candidate.getEmail(), subject, html);
            } catch (Exception e) {
                System.err.println("❌ Error sending candidate email: " + e.getMessage());
            }
        });

        // ✅ Send styled HTML email to recruiter
        userRepo.findById(interview.getRecruiterId()).ifPresent(recruiter -> {
            try {
                String subject = "Interview Scheduled Confirmation – Zidio Connect";
                String html = """
                    <html><body style='font-family:Arial,sans-serif;background:#f8fafc;padding:20px;'>
                      <table align='center' width='600' style='background:#fff;border-radius:10px;box-shadow:0 3px 8px rgba(0,0,0,0.1);padding:30px;'>
                        <tr>
                          <td align='center'>
                            <h2 style='color:#0f172a;'>Zidio Connect</h2>
                            <h3 style='color:#14b8a6;'>Interview Confirmation</h3>
                            <hr style='border:none;height:1px;background:#e5e7eb;margin:20px 0;'/>
                          </td>
                        </tr>
                        <tr>
                          <td>
                            <p>Hi <b>%s</b>,</p>
                            <p>You have scheduled a new interview with candidate ID <b>%d</b>.</p>
                            <table width='100%%' style='margin-top:10px;margin-bottom:10px;border-collapse:collapse;'>
                              <tr><td style='padding:5px 0;'>📅 <b>Date & Time:</b></td><td>%s</td></tr>
                              <tr><td style='padding:5px 0;'>💻 <b>Mode:</b></td><td>%s</td></tr>
                              <tr><td style='padding:5px 0;'>🔗 <b>Link / Location:</b></td><td><a href='%s'>%s</a></td></tr>
                            </table>
                            <p>Please ensure all interview details are communicated clearly to the candidate.</p>
                            <p style='margin-top:20px;'>Best Regards,<br/><b>Zidio Connect Team</b></p>
                            <hr style='border:none;height:1px;background:#e5e7eb;margin:20px 0;'/>
                            <small style='color:#94a3b8;'>This is an automated message. Do not reply to this email.</small>
                          </td>
                        </tr>
                      </table>
                    </body></html>
                """.formatted(
                        recruiter.getFullName(),
                        interview.getCandidateId(),
                        interview.getScheduledAt(),
                        interview.getMode(),
                        interview.getLocation(),
                        interview.getLocation()
                );
                emailService.sendHtmlEmail(recruiter.getEmail(), subject, html);
            } catch (Exception e) {
                System.err.println("❌ Error sending recruiter email: " + e.getMessage());
            }
        });

        return ResponseEntity.ok("Interview scheduled & HTML emails sent!");
    }
}


