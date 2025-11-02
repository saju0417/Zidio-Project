package com.zidio.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name="applications")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Application {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    private Long userId;
    private Long jobId;
    private String status = "APPLIED";
    private LocalDateTime appliedAt = LocalDateTime.now();
}
