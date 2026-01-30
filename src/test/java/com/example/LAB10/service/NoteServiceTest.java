package com.example.LAB10.service;

import com.example.LAB10.dto.NoteDto;
import com.example.LAB10.model.Note;
import com.example.LAB10.model.User;
import com.example.LAB10.repository.NoteRepository;
import com.example.LAB10.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@SuppressWarnings("null")
class NoteServiceTest {

    @Mock
    private NoteRepository noteRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private NoteService noteService;

    private User currentUser;
    private User otherUser;
    private Note testNote;

    @BeforeEach
    void setUp() {
        currentUser = new User();
        currentUser.setId(1L);
        currentUser.setUsername("currentuser");
        currentUser.setEmail("current@example.com");

        otherUser = new User();
        otherUser.setId(2L);
        otherUser.setUsername("otheruser");
        otherUser.setEmail("other@example.com");

        testNote = new Note();
        testNote.setId(1L);
        testNote.setTitle("Test Note");
        testNote.setContent("Test Content");
        testNote.setUser(currentUser);

        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("currentuser");
        when(userRepository.findByUsername("currentuser")).thenReturn(Optional.of(currentUser));
    }

    @Nested
    @DisplayName("createNote() tests")
    class CreateNoteTests {

        @Test
        @DisplayName("Should create note for current user")
        void createNote_shouldCreateNoteForCurrentUser() {
            NoteDto dto = new NoteDto();
            dto.setTitle("New Note");
            dto.setContent("New Content");

            when(noteRepository.save(any(Note.class))).thenAnswer(invocation -> {
                Note saved = invocation.getArgument(0);
                saved.setId(1L);
                return saved;
            });

            NoteDto result = noteService.createNote(dto);

            assertThat(result).isNotNull();
            assertThat(result.getTitle()).isEqualTo("New Note");
            assertThat(result.getContent()).isEqualTo("New Content");
            verify(noteRepository).save(any(Note.class));
        }
    }

    @Nested
    @DisplayName("getMyNotes() tests")
    class GetMyNotesTests {

        @Test
        @DisplayName("Should return only notes owned by current user")
        void getMyNotes_shouldReturnOnlyCurrentUserNotes() {
            Note note1 = new Note();
            note1.setId(1L);
            note1.setTitle("Note 1");
            note1.setContent("Content 1");
            note1.setUser(currentUser);

            Note note2 = new Note();
            note2.setId(2L);
            note2.setTitle("Note 2");
            note2.setContent("Content 2");
            note2.setUser(currentUser);

            when(noteRepository.findByUser(currentUser)).thenReturn(Arrays.asList(note1, note2));

            List<NoteDto> result = noteService.getMyNotes();

            assertThat(result).hasSize(2);
            assertThat(result).extracting(NoteDto::getTitle).containsExactly("Note 1", "Note 2");
        }

        @Test
        @DisplayName("Should return empty list when user has no notes")
        void getMyNotes_shouldReturnEmptyListWhenNoNotes() {
            when(noteRepository.findByUser(currentUser)).thenReturn(List.of());

            List<NoteDto> result = noteService.getMyNotes();

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("getNoteById() tests - Access Control")
    class GetNoteByIdTests {

        @Test
        @DisplayName("Should return note when owned by current user")
        void getNoteById_shouldReturnNoteWhenOwnedByCurrentUser() {
            when(noteRepository.findById(1L)).thenReturn(Optional.of(testNote));

            NoteDto result = noteService.getNoteById(1L);

            assertThat(result).isNotNull();
            assertThat(result.getTitle()).isEqualTo("Test Note");
        }

        @Test
        @DisplayName("Should throw AccessDeniedException when note belongs to another user")
        void getNoteById_shouldThrowAccessDeniedForOtherUsersNote() {
            testNote.setUser(otherUser);
            when(noteRepository.findById(1L)).thenReturn(Optional.of(testNote));

            assertThatThrownBy(() -> noteService.getNoteById(1L))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessageContaining("do not have permission");
        }

        @Test
        @DisplayName("Should throw NoSuchElementException when note not found")
        void getNoteById_shouldThrowExceptionWhenNotFound() {
            when(noteRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> noteService.getNoteById(999L))
                    .isInstanceOf(NoSuchElementException.class)
                    .hasMessage("Note not found");
        }
    }

    @Nested
    @DisplayName("updateNote() tests - Access Control")
    class UpdateNoteTests {

        @Test
        @DisplayName("Should update note when owned by current user")
        void updateNote_shouldUpdateWhenOwnedByCurrentUser() {
            NoteDto updateDto = new NoteDto();
            updateDto.setTitle("Updated Title");
            updateDto.setContent("Updated Content");

            when(noteRepository.findById(1L)).thenReturn(Optional.of(testNote));
            when(noteRepository.save(any(Note.class))).thenAnswer(invocation -> invocation.getArgument(0));

            NoteDto result = noteService.updateNote(1L, updateDto);

            assertThat(result.getTitle()).isEqualTo("Updated Title");
            assertThat(result.getContent()).isEqualTo("Updated Content");
        }

        @Test
        @DisplayName("Should throw AccessDeniedException when updating another user's note")
        void updateNote_shouldThrowAccessDeniedForOtherUsersNote() {
            testNote.setUser(otherUser);
            when(noteRepository.findById(1L)).thenReturn(Optional.of(testNote));

            NoteDto updateDto = new NoteDto();
            updateDto.setTitle("Hacked Title");

            assertThatThrownBy(() -> noteService.updateNote(1L, updateDto))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessageContaining("cannot update someone else's note");
        }
    }

    @Nested
    @DisplayName("partialUpdateNote() tests")
    class PartialUpdateNoteTests {

        @Test
        @DisplayName("Should update only title when only title provided")
        void partialUpdateNote_shouldUpdateOnlyTitle() {
            NoteDto updateDto = new NoteDto();
            updateDto.setTitle("New Title Only");

            when(noteRepository.findById(1L)).thenReturn(Optional.of(testNote));
            when(noteRepository.save(any(Note.class))).thenAnswer(invocation -> invocation.getArgument(0));

            NoteDto result = noteService.partialUpdateNote(1L, updateDto);

            assertThat(result.getTitle()).isEqualTo("New Title Only");
            assertThat(result.getContent()).isEqualTo("Test Content");
        }

        @Test
        @DisplayName("Should update only content when only content provided")
        void partialUpdateNote_shouldUpdateOnlyContent() {
            NoteDto updateDto = new NoteDto();
            updateDto.setContent("New Content Only");

            when(noteRepository.findById(1L)).thenReturn(Optional.of(testNote));
            when(noteRepository.save(any(Note.class))).thenAnswer(invocation -> invocation.getArgument(0));

            NoteDto result = noteService.partialUpdateNote(1L, updateDto);

            assertThat(result.getTitle()).isEqualTo("Test Note");
            assertThat(result.getContent()).isEqualTo("New Content Only");
        }
    }

    @Nested
    @DisplayName("deleteNote() tests - Access Control")
    class DeleteNoteTests {

        @Test
        @DisplayName("Should delete note when owned by current user")
        void deleteNote_shouldDeleteWhenOwnedByCurrentUser() {
            when(noteRepository.findById(1L)).thenReturn(Optional.of(testNote));

            noteService.deleteNote(1L);

            verify(noteRepository).delete(testNote);
        }

        @Test
        @DisplayName("Should throw AccessDeniedException when deleting another user's note")
        void deleteNote_shouldThrowAccessDeniedForOtherUsersNote() {
            testNote.setUser(otherUser);
            when(noteRepository.findById(1L)).thenReturn(Optional.of(testNote));

            assertThatThrownBy(() -> noteService.deleteNote(1L))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessageContaining("cannot delete someone else's note");

            verify(noteRepository, never()).delete(any());
        }

        @Test
        @DisplayName("Should throw NoSuchElementException when note not found")
        void deleteNote_shouldThrowExceptionWhenNotFound() {
            when(noteRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> noteService.deleteNote(999L))
                    .isInstanceOf(NoSuchElementException.class)
                    .hasMessage("Note not found");
        }
    }
}
