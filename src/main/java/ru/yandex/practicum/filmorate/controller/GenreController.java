package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.service.genre.GenreService;

import java.util.Collection;

/**
 * Контроллер для работы с жанрами фильмов.
 * Обрабатывает запросы к эндпоинтам /genres.
 */
@RestController
@RequestMapping("/genres")
@RequiredArgsConstructor
public class GenreController {

    private final GenreService genreService;

    @GetMapping
    public ResponseEntity<Collection<Genre>> retrieveAllGenres() {
        return ResponseEntity.ok(genreService.getAllGenres());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Genre> retrieveGenreById(
            @PathVariable @Positive(message = "Идентификатор должен быть положительным числом") Long id) {
        return ResponseEntity.ok(genreService.getGenreById(id));
    }
}