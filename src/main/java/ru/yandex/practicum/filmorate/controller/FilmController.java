package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.film.FilmService;

import java.util.Collection;

/**
 * Контроллер для управления фильмами.
 * Обрабатывает CRUD-операции и лайки.
 */
@Slf4j
@RestController
@RequestMapping("/films")
@RequiredArgsConstructor
public class FilmController {

    private final FilmService filmService;

    @GetMapping
    public Collection<Film> listAllFilms() {
        log.info("Запрошен список всех фильмов");
        return filmService.getAllFilms();
    }

    @GetMapping("/{id}")
    public Film fetchFilmById(@PathVariable @Positive(message = "Идентификатор фильма должен быть положительным") Long id) {
        log.debug("Запрос фильма с id={}", id);
        return filmService.getFilmById(id);
    }

    @GetMapping("/popular")
    public Collection<Film> fetchPopularFilms(
            @RequestParam(defaultValue = "10") @Positive(message = "Количество должно быть положительным") Integer count) {
        log.info("Запрошены популярные фильмы (limit={})", count);
        return filmService.getMostPopularFilms(count);
    }

    @GetMapping("/director/{directorId}")
    public Collection<Film> getFilmsByDirector(
            @PathVariable @Positive(message = "ID режиссёра должен быть положительным") Long directorId,
            @RequestParam(required = false) String sortBy) {

        log.info("Запрошены фильмы режиссёра {} с сортировкой {}", directorId, sortBy);

        if (sortBy != null && !sortBy.matches("year|likes")) {
            throw new ValidationException("Параметр sortBy должен быть 'year' или 'likes'");
        }

        return filmService.getFilmsByDirector(directorId, sortBy);
    }

    @PostMapping
    public Film registerFilm(@Valid @RequestBody Film film) {
        log.info("Создание нового фильма: \"{}\"", film.getName());
        return filmService.addFilm(film);
    }

    @PutMapping
    public Film modifyFilm(@Valid @RequestBody Film updatedFilm) {
        log.info("Обновление фильма с id={}", updatedFilm.getId());
        return filmService.editFilm(updatedFilm);
    }

    @PutMapping("/{id}/like/{userId}")
    public Film applyLike(
            @PathVariable @Positive(message = "ID фильма должен быть положительным") Long id,
            @PathVariable @Positive(message = "ID пользователя должен быть положительным") Long userId) {
        log.info("Пользователь {} ставит лайк фильму {}", userId, id);
        filmService.likeFilm(id, userId);
        return filmService.getFilmById(id);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public Film retractLike(
            @PathVariable @Positive(message = "ID фильма должен быть положительным") Long id,
            @PathVariable @Positive(message = "ID пользователя должен быть положительным") Long userId) {
        log.info("Пользователь {} убирает лайк у фильма {}", userId, id);
        filmService.unlikeFilm(id, userId);
        return filmService.getFilmById(id);
    }

    @DeleteMapping("/{id}")
    public void excludeFilm(@PathVariable @Positive(message = "ID должен быть положительным") Long id) {
        log.info("Удаление фильма с id={}", id);
        filmService.deleteFilm(id);
    }
}
