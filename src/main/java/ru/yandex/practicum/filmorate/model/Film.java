package ru.yandex.practicum.filmorate.model;

import lombok.Data;

import java.time.LocalDate;

/**
 * Film.
 */
@Data
public class Film {

/** Целочисленный идентификатор. */
    private Long id;

/** Название. */
    private String name;

/** Описание. */
    private String description;

/** Дата релиза. */
    private LocalDate releaseDate;

/** Продолжительность фильма. */
    private Integer duration;
}
