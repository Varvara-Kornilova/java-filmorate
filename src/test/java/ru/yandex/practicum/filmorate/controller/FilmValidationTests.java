package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class FilmValidationTests {

    private final FilmController filmController = new FilmController();

    @Test
    void shouldThrowOnNullName() {
        Film film = new Film();
        film.setName(null);
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(100);

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> filmController.validateFilm(film)
        );
        assertEquals("название не может быть пустым", exception.getMessage());
    }

    @Test
    void shouldThrowOnBlankName() {
        Film film = new Film();
        film.setName("   ");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(100);

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> filmController.validateFilm(film)
        );
        assertEquals("название не может быть пустым", exception.getMessage());
    }

    @Test
    void shouldThrowOnDescriptionLongerThan200() {
        Film film = new Film();
        film.setName("Film");
        film.setDescription("a".repeat(201)); // 201 символ
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(100);

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> filmController.validateFilm(film)
        );
        assertEquals("максимальная длина описания — 200 символов", exception.getMessage());
    }

    @Test
    void shouldAcceptDescriptionExactly200Chars() {
        Film film = new Film();
        film.setName("Film");
        film.setDescription("a".repeat(200)); // ровно 200
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(100);

        assertDoesNotThrow(() -> filmController.validateFilm(film));
    }

    @Test
    void shouldThrowOnReleaseDateBefore1895_12_28() {
        Film film = new Film();
        film.setName("Film");
        film.setReleaseDate(LocalDate.of(1895, 12, 27)); // на день раньше
        film.setDuration(100);

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> filmController.validateFilm(film)
        );
        assertEquals("дата релиза — не раньше 28 декабря 1895 года", exception.getMessage());
    }

    @Test
    void shouldAcceptReleaseDateOn1895_12_28() {
        Film film = new Film();
        film.setName("Film");
        film.setReleaseDate(LocalDate.of(1895, 12, 28)); // точная дата
        film.setDuration(100);

        assertDoesNotThrow(() -> filmController.validateFilm(film));
    }

    @Test
    void shouldThrowOnZeroDuration() {
        Film film = new Film();
        film.setName("Film");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(0);

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> filmController.validateFilm(film)
        );
        assertEquals("продолжительность фильма должна быть положительным числом", exception.getMessage());
    }

    @Test
    void shouldThrowOnNegativeDuration() {
        Film film = new Film();
        film.setName("Film");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(-5);

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> filmController.validateFilm(film)
        );
        assertEquals("продолжительность фильма должна быть положительным числом", exception.getMessage());
    }

    @Test
    void shouldAcceptValidFilm() {
        Film film = new Film();
        film.setName("Film");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(2010, 7, 16));
        film.setDuration(148);

        assertDoesNotThrow(() -> filmController.validateFilm(film));
    }

    @Test
    void shouldAcceptNullDescription() {
        Film film = new Film();
        film.setName("GoodFilm");
        film.setDescription(null);
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(100);

        assertDoesNotThrow(() -> filmController.validateFilm(film));
    }
}