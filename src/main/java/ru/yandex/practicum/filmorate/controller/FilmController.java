package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
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
@Validated
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
            @RequestParam(defaultValue = "10") @Positive(message = "Количество должно быть положительным") Integer count,
            @RequestParam(required = false) Long genreId,
            @RequestParam(required = false) Integer year) {

        log.info("Запрошены популярные фильмы (limit={}, genreId={}, year={})", count, genreId, year);

        return filmService.getMostPopularFilms(count, genreId, year);
    }

    @GetMapping("/search")
    public Collection<Film> searchFilms(
            @RequestParam(required = true) String query,
            @RequestParam(required = true) String by) {

        log.info("Поиск фильмов: query={}, by={}", query, by);

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

        log.info("Запрошены фильмы режиссёра {} с сортировкой {}", directorId, sortBy);

        if (sortBy != null && !sortBy.matches("year|likes")) {
            throw new ValidationException("Параметр sortBy должен быть 'year' или 'likes'");
        }

        return filmService.getFilmsByDirector(directorId, sortBy);
    }

    // НОВЫЙ ЭНДПОИНТ
    @GetMapping("/common")
    public Collection<Film> getCommonFilms(
            @RequestParam @Positive(message = "ID пользователя должен быть положительным") Long userId,
            @RequestParam @Positive(message = "ID друга должен быть положительным") Long friendId) {
        log.info("Запрошены общие фильмы пользователей {} и {}", userId, friendId);
        return filmService.getCommonFilms(userId, friendId);
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
