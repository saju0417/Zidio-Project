package com.zidio.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "interviews")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Interview {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long jobId;
    private Long candidateId;
    private Long recruiterId;
    private LocalDateTime scheduledAt;
    private String mode;
    private String location;
    private String status = "SCHEDULED";
}
