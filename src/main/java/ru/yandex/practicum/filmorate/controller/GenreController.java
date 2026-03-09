package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.service.genre.GenreService;

import java.util.Collection;

@Slf4j
@RestController
@RequestMapping("/genres")
@RequiredArgsConstructor
public class GenreController {

    private final GenreService genreService;

    @GetMapping
    public Collection<Genre> retrieveAllGenres() {
        log.info("Запрошен список всех жанров");
        return genreService.getAllGenres();
    }

    @GetMapping("/{id}")
    public Genre retrieveGenreById(@PathVariable @Positive(message = "Идентификатор должен быть положительным числом") Long id) {
        log.debug("Запрошен жанр с идентификатором: {}", id);
        return genreService.getGenreById(id);
    }
}
