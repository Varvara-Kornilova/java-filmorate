package ru.yandex.practicum.filmorate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.film.FilmService;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FilmController.class)
public class FilmControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private FilmService filmService;

    private Film testFilm;

    @BeforeEach
    public void setUp() {
        testFilm = Film.builder()
                .id(1L)
                .name("Test Film")
                .description("Test Description")
                .releaseDate(LocalDate.of(2024, 1, 1))
                .duration(100)
                .build();
    }

    @Test
    public void searchFilms_ByTitle_ReturnsMatchingFilms() throws Exception {
        List<Film> expectedFilms = List.of(testFilm);
        when(filmService.searchFilms(eq("Test"), eq("title")))
                .thenReturn(expectedFilms);

        mockMvc.perform(get("/films/search")
                        .param("query", "Test")
                        .param("by", "title"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Test Film"));
    }

    @Test
    public void searchFilms_InvalidByParameter_ThrowsValidationException() throws Exception {
        mockMvc.perform(get("/films/search")
                        .param("query", "test")
                        .param("by", "invalid"))
                .andExpect(status().isBadRequest()); // или isInternalServerError(), зависит от обработки
    }

    @Test
    public void registerFilm_Created_ReturnsCreatedStatus() throws Exception {
        when(filmService.addFilm(any(Film.class))).thenReturn(testFilm);

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testFilm)))
                .andExpect(status().isCreated()) // 201 Created
                .andExpect(jsonPath("$.name").value("Test Film"));
    }

    @Test
    public void getCommonFilms_ReturnsCommonFilms() throws Exception {
        List<Film> commonFilms = List.of(testFilm);
        when(filmService.getCommonFilms(eq(1L), eq(2L)))
                .thenReturn(commonFilms);

        mockMvc.perform(get("/films/common")
                        .param("userId", "1")
                        .param("friendId", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }
}