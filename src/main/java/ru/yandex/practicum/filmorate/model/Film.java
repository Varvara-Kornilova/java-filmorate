package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.Data;
import ru.yandex.practicum.filmorate.validator.ValidReleaseDate;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * Film.
 */
@Data
public class Film {

    /** Целочисленный идентификатор. */
    private Long id;

    /** Название. */
    @NotBlank(message = "Название фильма не может быть пустым")
    private String name;

    /** Описание. */
    @Size(max = 200, message = "Максимальная длина описания — 200 символов")
    private String description;

    /** Дата релиза. */
    @ValidReleaseDate
    @PastOrPresent(message = "Дата релиза не может быть в будущем")
    private LocalDate releaseDate;

    /** Продолжительность фильма. */
    @Positive(message = "Продолжительность фильма должна быть положительным числом")
    private Integer duration;

    /** Лайки у фильма. */
    private Set<Long> likes = new HashSet<>();

    /** Возрастной рейтинг фильма. */
    private Mpa mpa;

    /** Жанры, к которым фильм относится. */
    private Set<Genre> genres = new HashSet<>();
}
