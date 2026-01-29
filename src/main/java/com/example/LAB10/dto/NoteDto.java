package com.example.LAB10.dto;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class NoteDto {
    private Long id;

    @NotBlank(message = "Title required")
    private String title;

    private String content;
}