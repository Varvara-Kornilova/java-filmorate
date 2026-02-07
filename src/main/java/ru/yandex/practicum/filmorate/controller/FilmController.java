package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.film.FilmService;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/films")
@RequiredArgsConstructor
public class FilmController {
    private final FilmStorage filmStorage;
    private final FilmService filmService;

    @GetMapping
    public List<Film> findAllFilms() {
        log.debug("Запрос на получение всех фильмов");
        List<Film> films = filmStorage.findAllFilms();
        log.debug("Найдено {} фильмов", films.size());
        return films;
    }

    @GetMapping("/popular")
    public List<Film> getTheMostPopularFilms(@RequestParam(defaultValue = "10") int count) {
        log.debug("Запрос на получение {} популярных фильмов", count);
        List<Film> films = filmService.getTheMostPopularFilms(count);
        log.debug("Возвращено {} популярных фильмов", films.size());
        return films;
    }

    @PostMapping
    public Film create(@Valid @RequestBody Film film) {
        log.info("Создание нового фильма: {}", film.getName());
        Film createdFilm = filmStorage.create(film);
        log.info("Фильм успешно создан с id = {}", createdFilm.getId());
        return createdFilm;
    }

    @PutMapping
    public Film update(@Valid @RequestBody Film newFilm) {
        log.info("Обновление фильма с id = {}", newFilm.getId());
        Film updatedFilm = filmStorage.update(newFilm);
        log.info("Фильм с id = {} успешно обновлён", updatedFilm.getId());
        return updatedFilm;
    }

    @PutMapping("/{id}/like/{userId}")
    public Film addLike(@PathVariable("id") Long filmId, @PathVariable("userId") Long userId) {
        log.info("Пользователь {} ставит лайк фильму {}", userId, filmId);
        Film film = filmService.addLike(filmId, userId);
        log.info("Лайк от пользователя {} к фильму {} добавлен", userId, filmId);
        return film;
    }

    @DeleteMapping("/{id}/like/{userId}")
    public Film removeLike(@PathVariable("id") Long filmId, @PathVariable("userId") Long userId) {
        log.info("Пользователь {} удаляет лайк у фильма {}", userId, filmId);
        Film film = filmService.removeLike(filmId, userId);
        log.info("Лайк от пользователя {} к фильму {} удалён", userId, filmId);
        return film;
    }
}
