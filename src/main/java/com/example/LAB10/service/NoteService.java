package com.example.LAB10.service;

import com.example.LAB10.dto.NoteDto;
import com.example.LAB10.model.Note;
import com.example.LAB10.model.User;
import com.example.LAB10.repository.NoteRepository;
import com.example.LAB10.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NoteService {

    private final NoteRepository noteRepo;
    private final UserRepository userRepo;
    private final JdbcTemplate jdbcTemplate;

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepo.findByUsername(username)
                .orElseThrow(() -> new NoSuchElementException("User not found"));
    }

    public NoteDto createNote(NoteDto dto) {
        User user = getCurrentUser();
        Note note = new Note();
        note.setTitle(dto.getTitle());
        note.setContent(dto.getContent());
        note.setUser(user);
        return mapToDto(noteRepo.save(note));
    }

    public List<NoteDto> getMyNotes() {
        User user = getCurrentUser();
        return noteRepo.findByUser(user).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public int countMyNotesRawSql() {
        User user = getCurrentUser();
        String sql = "SELECT COUNT(*) FROM notes WHERE user_id = ?";
        return jdbcTemplate.queryForObject(sql, Integer.class, user.getId());
    }

    public NoteDto getNoteById(Long id) {
        Note note = noteRepo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Note not found"));

        if (!note.getUser().getId().equals(getCurrentUser().getId())) {
            throw new AccessDeniedException("You do not have permission to access this note");
        }
        return mapToDto(note);
    }

    public void deleteNote(Long id) {
        Note note = noteRepo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Note not found"));

        if (!note.getUser().getId().equals(getCurrentUser().getId())) {
            throw new AccessDeniedException("You cannot delete someone else's note");
        }
        noteRepo.delete(note);
    }

    private NoteDto mapToDto(Note note) {
        NoteDto dto = new NoteDto();
        dto.setId(note.getId());
        dto.setTitle(note.getTitle());
        dto.setContent(note.getContent());
        return dto;
    }
}