package ru.yandex.practicum.filmorate.model;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class FilmValidationTests {

    @Autowired
    private Validator validator;

    private Film createValidFilm() {
        Film film = new Film();
        film.setName("Inception");
        film.setDescription("A thief who steals secrets...");
        film.setReleaseDate(LocalDate.of(2010, 7, 16));
        film.setDuration(148);
        return film;
    }

    @Test
    public void shouldHaveNoViolationsForValidFilm() {
        Film film = createValidFilm();
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertTrue(violations.isEmpty());
    }

    @Test
    public void shouldRejectNullName() {
        Film film = createValidFilm();
        film.setName(null);
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertEquals(1, violations.size());
        assertEquals("Название фильма не может быть пустым", violations.iterator().next().getMessage());
    }

    @Test
    public void shouldRejectBlankName() {
        Film film = createValidFilm();
        film.setName("   ");
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertEquals(1, violations.size());
        assertEquals("Название фильма не может быть пустым", violations.iterator().next().getMessage());
    }

    @Test
    public void shouldRejectDescriptionLongerThan200() {
        Film film = createValidFilm();
        film.setDescription("a".repeat(201));
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertEquals(1, violations.size());
        assertEquals("Максимальная длина описания — 200 символов", violations.iterator().next().getMessage());
    }

    @Test
    public void shouldAcceptDescriptionExactly200Chars() {
        Film film = createValidFilm();
        film.setDescription("a".repeat(200));
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertTrue(violations.isEmpty());
    }

    @Test
    public void shouldRejectFutureReleaseDate() {
        Film film = createValidFilm();
        film.setReleaseDate(LocalDate.now().plusDays(1));
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertEquals(1, violations.size());
        assertEquals("Дата релиза не может быть в будущем", violations.iterator().next().getMessage());
    }

    @Test
    public void shouldRejectReleaseDateBefore1895_12_28() {
        Film film = createValidFilm();
        film.setReleaseDate(LocalDate.of(1895, 12, 27));
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertEquals(1, violations.size());
        assertEquals("дата релиза — не раньше 28 декабря 1895 года", violations.iterator().next().getMessage());
    }

    @Test
    public void shouldAcceptReleaseDateOn1895_12_28() {
        Film film = createValidFilm();
        film.setReleaseDate(LocalDate.of(1895, 12, 28));
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertTrue(violations.isEmpty());
    }

    @Test
    public void shouldRejectZeroDuration() {
        Film film = createValidFilm();
        film.setDuration(0);
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertEquals(1, violations.size());
        assertEquals("Продолжительность фильма должна быть положительным числом", violations.iterator().next().getMessage());
    }

    @Test
    public void shouldRejectNegativeDuration() {
        Film film = createValidFilm();
        film.setDuration(-5);
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertEquals(1, violations.size());
        assertEquals("Продолжительность фильма должна быть положительным числом", violations.iterator().next().getMessage());
    }
}
