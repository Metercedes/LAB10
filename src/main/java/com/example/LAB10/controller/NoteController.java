package com.example.LAB10.controller;

import com.example.LAB10.dto.NoteDto;
import com.example.LAB10.service.NoteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/notes")
@RequiredArgsConstructor
public class NoteController {

    private final NoteService noteService;

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<NoteDto> createNote(@Valid @RequestBody NoteDto dto) {
        return ResponseEntity.ok(noteService.createNote(dto));
    }

    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<NoteDto>> getMyNotes() {
        return ResponseEntity.ok(noteService.getMyNotes());
    }

    @GetMapping("/count")
    public ResponseEntity<Integer> countNotes() {
        return ResponseEntity.ok(noteService.countMyNotesRawSql());
    }

    @GetMapping("/{id}")
    public ResponseEntity<NoteDto> getNote(@PathVariable Long id) {
        return ResponseEntity.ok(noteService.getNoteById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNote(@PathVariable Long id) {
        noteService.deleteNote(id);
        return ResponseEntity.noContent().build();
    }
}