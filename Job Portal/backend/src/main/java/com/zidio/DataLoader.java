package com.zidio;

import com.zidio.entity.User;
import com.zidio.entity.Job;
import com.zidio.entity.Interview;
import com.zidio.repository.UserRepository;
import com.zidio.repository.JobRepository;
import com.zidio.repository.InterviewRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

@Component
public class DataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final JobRepository jobRepository;
    private final InterviewRepository interviewRepository;

    public DataLoader(UserRepository userRepository, JobRepository jobRepository, InterviewRepository interviewRepository) {
        this.userRepository = userRepository;
        this.jobRepository = jobRepository;
        this.interviewRepository = interviewRepository;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) return;

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        User admin = new User(null, "admin", "admin@example.com", encoder.encode("admin123"), "ADMIN", "Admin User", null, null, true);
        User recruiter = new User(null, "recruiter", "recruiter@example.com", encoder.encode("recruiter123"), "RECRUITER", "Recruiter Rick", null, null, true);
        User student = new User(null, "student", "student@example.com", encoder.encode("student123"), "STUDENT", "Student Sam", null, null, true);
        userRepository.save(admin);
        userRepository.save(recruiter);
        userRepository.save(student);

        Job j1 = new Job(null, "Frontend Developer", "React + Tailwind experience", "NextGen", "Remote", recruiter.getId());
        Job j2 = new Job(null, "Backend Intern", "Java Spring Boot internship", "NextGen", "Chennai", recruiter.getId());
        jobRepository.save(j1);
        jobRepository.save(j2);

        Interview interview = new Interview(null, j1.getId(), student.getId(), recruiter.getId(), LocalDateTime.now().plusDays(3), "Zoom", "https://zoom.us/demo", "SCHEDULED");
        interviewRepository.save(interview);
    }
}
