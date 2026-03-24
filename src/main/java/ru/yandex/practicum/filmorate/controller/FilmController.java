package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.film.FilmService;

import java.util.Collection;

/**
 * Контроллер для управления фильмами.
 * Обрабатывает CRUD-операции и запросы к эндпоинтам /films.
 */
@RestController
@RequestMapping("/films")
@RequiredArgsConstructor
@Validated
public class FilmController {

    private final FilmService filmService;

    @GetMapping
    public ResponseEntity<Collection<Film>> listAllFilms() {
        return ResponseEntity.ok(filmService.getAllFilms());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Film> fetchFilmById(
            @PathVariable @Positive(message = "Идентификатор фильма должен быть положительным") Long id) {
        return ResponseEntity.ok(filmService.getFilmById(id));
    }

    @GetMapping("/popular")
    public ResponseEntity<Collection<Film>> fetchPopularFilms(
            @RequestParam(defaultValue = "10") @Positive(message = "Количество должно быть положительным") Integer count,
            @RequestParam(required = false) Long genreId,
            @RequestParam(required = false) Integer year) {
        return ResponseEntity.ok(filmService.getMostPopularFilms(count, genreId, year));
    }

    @GetMapping("/search")
    public ResponseEntity<Collection<Film>> searchFilms(
            @RequestParam(required = true) String query,
            @RequestParam(required = true) String by) {

        if (by != null && !by.matches("(title|director)(,(title|director))?")) {
            throw new ValidationException(
                    "Параметр 'by' должен принимать значения: title, director или title,director");
        }

        return ResponseEntity.ok(filmService.searchFilms(query, by));
    }

    @GetMapping("/director/{directorId}")
    public ResponseEntity<Collection<Film>> getFilmsByDirector(
            @PathVariable @Positive(message = "ID режиссёра должен быть положительным") Long directorId,
            @RequestParam(required = false) String sortBy) {

        if (sortBy != null && !sortBy.matches("year|likes")) {
            throw new ValidationException("Параметр sortBy должен быть 'year' или 'likes'");
        }

        return ResponseEntity.ok(filmService.getFilmsByDirector(directorId, sortBy));
    }

    @GetMapping("/common")
    public ResponseEntity<Collection<Film>> getCommonFilms(
            @RequestParam @Positive(message = "ID пользователя должен быть положительным") Long userId,
            @RequestParam @Positive(message = "ID друга должен быть положительным") Long friendId) {
        return ResponseEntity.ok(filmService.getCommonFilms(userId, friendId));
    }

    @PostMapping
    public ResponseEntity<Film> registerFilm(@Valid @RequestBody Film film) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(filmService.addFilm(film));
    }

    @PutMapping
    public ResponseEntity<Film> modifyFilm(@Valid @RequestBody Film updatedFilm) {
        return ResponseEntity.ok(filmService.editFilm(updatedFilm));
    }

    @PutMapping("/{id}/like/{userId}")
    public ResponseEntity<Film> applyLike(
            @PathVariable @Positive(message = "ID фильма должен быть положительным") Long id,
            @PathVariable @Positive(message = "ID пользователя должен быть положительным") Long userId) {
        filmService.likeFilm(id, userId);
        return ResponseEntity.ok(filmService.getFilmById(id));
    }

    @DeleteMapping("/{id}/like/{userId}")
    public ResponseEntity<Film> retractLike(
            @PathVariable @Positive(message = "ID фильма должен быть положительным") Long id,
            @PathVariable @Positive(message = "ID пользователя должен быть положительным") Long userId) {
        filmService.unlikeFilm(id, userId);
        return ResponseEntity.ok(filmService.getFilmById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excludeFilm(
            @PathVariable @Positive(message = "ID должен быть положительным") Long id) {
        filmService.deleteFilm(id);
        return ResponseEntity.noContent().build();
    }
}