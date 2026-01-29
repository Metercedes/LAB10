package com.example.LAB10.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name="notes")
@Data
public class Note {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false)
    private String title;

    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}