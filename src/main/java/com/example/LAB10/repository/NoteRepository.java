package com.example.LAB10.repository;
import com.example.LAB10.model.Note;
import com.example.LAB10.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NoteRepository extends JpaRepository<Note, Long> {
    List<Note> findByUser(User user);
}