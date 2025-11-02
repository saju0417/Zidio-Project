package com.zidio.controller;

import com.zidio.entity.Job;
import com.zidio.entity.User;
import com.zidio.repository.JobRepository;
import com.zidio.repository.UserRepository;
import com.zidio.service.ResumeService;
import com.zidio.util.SecurityUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/resume")
public class ResumeController {
    private final ResumeService resumeService;
    private final UserRepository userRepository;
    private final JobRepository jobRepository;

    public ResumeController(ResumeService resumeService, UserRepository userRepository, JobRepository jobRepository) {
        this.resumeService = resumeService; this.userRepository = userRepository; this.jobRepository = jobRepository;
    }

    @PostMapping(value = "/upload")
    public ResponseEntity<?> uploadResume(@RequestParam("file") MultipartFile file) {
        try {
            User me = SecurityUtils.getCurrentUser();
            if (me == null) return ResponseEntity.status(401).body("Unauthorized");
            String filename = "resume_user_"+me.getId()+"_"+System.currentTimeMillis()+".pdf";
            String text = resumeService.saveAndExtractText(file, filename);
            me.setResumePath(filename); me.setResumeText(text); userRepository.save(me);
            return ResponseEntity.ok("Uploaded and extracted");
        } catch (Exception e) { return ResponseEntity.status(500).body("Error processing file"); }
    }

    @GetMapping("/view")
    public ResponseEntity<?> viewMyResume() {
        User me = SecurityUtils.getCurrentUser();
        if (me == null) return ResponseEntity.status(401).body("Unauthorized");
        Map<String,Object> res = new HashMap<>(); res.put("resumeText", me.getResumeText()); res.put("resumePath", me.getResumePath());
        return ResponseEntity.ok(res);
    }

    @GetMapping("/download")
    public ResponseEntity<?> downloadMyResume() {
        User me = SecurityUtils.getCurrentUser();
        if (me == null) return ResponseEntity.status(401).body("Unauthorized");
        if (me.getResumePath() == null) return ResponseEntity.notFound().build();
        try {
            Path p = resumeService.getUploadDir().resolve(me.getResumePath());
            Resource resource = new UrlResource(p.toUri());
            if (!resource.exists()) return ResponseEntity.notFound().build();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentDisposition(ContentDisposition.builder("attachment").filename(me.getResumePath()).build());
            headers.setContentType(MediaType.APPLICATION_PDF);
            return new ResponseEntity<>(resource, headers, HttpStatus.OK);
        } catch (MalformedURLException e) { return ResponseEntity.status(500).body("File error"); }
    }

    @GetMapping("/match")
    public ResponseEntity<?> matchJobsForMe() {
        User me = SecurityUtils.getCurrentUser();
        if (me == null) return ResponseEntity.status(401).body("Unauthorized");
        String resumeText = me.getResumeText();
        List<Job> jobs = jobRepository.findAll();
        List<Map<String,Object>> results = jobs.stream().map(job -> {
            double sim = resumeService.calculateSimilarity(resumeText==null?"":resumeText, job.getDescription()==null?job.getTitle():job.getTitle()+" "+job.getDescription());
            Map<String,Object> m = new HashMap<>();
            m.put("jobId", job.getId()); m.put("title", job.getTitle()); m.put("company", job.getCompany()); m.put("location", job.getLocation());
            m.put("score", (int)Math.round(sim*100)); return m;
        }).sorted((a,b)-> Integer.compare((Integer)b.get("score"),(Integer)a.get("score"))).collect(Collectors.toList());
        return ResponseEntity.ok(results);
    }
}
