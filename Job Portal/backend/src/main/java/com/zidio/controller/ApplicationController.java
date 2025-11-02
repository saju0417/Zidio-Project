package com.zidio.controller;

import com.zidio.entity.Application;
import com.zidio.entity.User;
import com.zidio.repository.ApplicationRepository;
import com.zidio.util.SecurityUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/applications")
public class ApplicationController {
    private final ApplicationRepository applicationRepository;
    public ApplicationController(ApplicationRepository applicationRepository) { this.applicationRepository = applicationRepository; }

    @PostMapping("/apply")
    public ResponseEntity<?> apply(@RequestBody Application app) {
        User me = SecurityUtils.getCurrentUser();
        if (me == null) return ResponseEntity.status(401).body("Unauthorized");
        app.setUserId(me.getId());
        if (applicationRepository.existsByUserIdAndJobId(app.getUserId(), app.getJobId())) return ResponseEntity.badRequest().body("Already applied");
        Application saved = applicationRepository.save(app);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/byUser")
    public List<Application> myApplications() {
        User me = SecurityUtils.getCurrentUser();
        if (me == null) return List.of();
        return applicationRepository.findByUserId(me.getId());
    }
}
