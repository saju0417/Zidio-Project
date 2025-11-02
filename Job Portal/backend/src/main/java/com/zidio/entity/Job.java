package com.zidio.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name="jobs")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Job {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    private String title;
    @Column(columnDefinition="TEXT")
    private String description;
    private String company;
    private String location;
    private Long postedBy;
}
