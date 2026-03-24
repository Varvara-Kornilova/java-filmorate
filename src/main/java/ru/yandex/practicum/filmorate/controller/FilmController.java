package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
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
    public Collection<Film> listAllFilms() {
        return filmService.getAllFilms();
    }

    @GetMapping("/{id}")
    public Film fetchFilmById(@PathVariable @Positive(message = "Идентификатор фильма должен быть положительным") Long id) {
        return filmService.getFilmById(id);
    }

    @GetMapping("/popular")
    public Collection<Film> fetchPopularFilms(
            @RequestParam(defaultValue = "10") @Positive(message = "Количество должно быть положительным") Integer count,
            @RequestParam(required = false) Long genreId,
            @RequestParam(required = false) Integer year) {
        return filmService.getMostPopularFilms(count, genreId, year);
    }

    @GetMapping("/search")
    public Collection<Film> searchFilms(
            @RequestParam(required = true) String query,
            @RequestParam(required = true) String by) {

        if (by != null && !by.matches("(title|director)(,(title|director))?")) {
            throw new ValidationException(
                    "Параметр 'by' должен принимать значения: title, director или title,director");
        }

        return filmService.searchFilms(query, by);
    }

    @GetMapping("/director/{directorId}")
    public Collection<Film> getFilmsByDirector(
            @PathVariable @Positive(message = "ID режиссёра должен быть положительным") Long directorId,
            @RequestParam(required = false) String sortBy) {

        if (sortBy != null && !sortBy.matches("year|likes")) {
            throw new ValidationException("Параметр sortBy должен быть 'year' или 'likes'");
        }

        return filmService.getFilmsByDirector(directorId, sortBy);
    }

    @GetMapping("/common")
    public Collection<Film> getCommonFilms(
            @RequestParam @Positive(message = "ID пользователя должен быть положительным") Long userId,
            @RequestParam @Positive(message = "ID друга должен быть положительным") Long friendId) {
        return filmService.getCommonFilms(userId, friendId);
    }

    @PostMapping
    public Film registerFilm(@Valid @RequestBody Film film) {
        return filmService.addFilm(film);
    }

    @PutMapping
    public Film modifyFilm(@Valid @RequestBody Film updatedFilm) {
        return filmService.editFilm(updatedFilm);
    }

    @PutMapping("/{id}/like/{userId}")
    public Film applyLike(
            @PathVariable @Positive(message = "ID фильма должен быть положительным") Long id,
            @PathVariable @Positive(message = "ID пользователя должен быть положительным") Long userId) {
        filmService.likeFilm(id, userId);
        return filmService.getFilmById(id);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public Film retractLike(
            @PathVariable @Positive(message = "ID фильма должен быть положительным") Long id,
            @PathVariable @Positive(message = "ID пользователя должен быть положительным") Long userId) {
        filmService.unlikeFilm(id, userId);
        return filmService.getFilmById(id);
    }

    @DeleteMapping("/{id}")
    public void excludeFilm(@PathVariable @Positive(message = "ID должен быть положительным") Long id) {
        filmService.deleteFilm(id);
    }
}