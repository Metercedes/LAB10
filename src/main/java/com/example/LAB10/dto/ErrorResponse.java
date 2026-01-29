package com.example.LAB10.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
@Data @AllArgsConstructor
public class ErrorResponse { private String error; private String details; }