package com.zidio.controller;

import com.zidio.entity.Job;
import com.zidio.entity.User;
import com.zidio.repository.JobRepository;
import com.zidio.repository.UserRepository;
import com.zidio.util.SecurityUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/jobs")
public class JobController {
    private final JobRepository jobRepo;
    private final UserRepository userRepo;
    public JobController(JobRepository jobRepo, UserRepository userRepo) { this.jobRepo = jobRepo; this.userRepo = userRepo; }

    @GetMapping
    public List<Job> all() { return jobRepo.findAll(); }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Job job) {
        User me = SecurityUtils.getCurrentUser();
        if (me == null) return ResponseEntity.status(401).body("Unauthorized");
        if (!"RECRUITER".equalsIgnoreCase(me.getRole()) && !"ADMIN".equalsIgnoreCase(me.getRole()))
            return ResponseEntity.status(403).body("Only recruiters or admins can post jobs");
        job.setPostedBy(me.getId());
        Job saved = jobRepo.save(job);
        return ResponseEntity.ok(saved);
    }
}
