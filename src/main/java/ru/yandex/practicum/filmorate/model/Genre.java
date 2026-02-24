package ru.yandex.practicum.filmorate.model;

import lombok.Getter;

@Getter
public enum Genre {
    COMEDY(1, "Комедия"),
    DRAMA(2, "Драма"),
    CARTOON(3, "Мультфильм"),
    THRILLER(4, "Триллер"),
    DOCUMENTARY(5, "Документальный"),
    ACTION(6, "Боевик");

    private final long id;
    private final String name;

    Genre(long id, String name) {
        this.id = id;
        this.name = name;
    }

    public static Genre getById(long id) {
        for (Genre genre : values()) {
            if (genre.id == id) {
                return genre;
            }
        }
        throw new IllegalArgumentException("Жанр с id " + id + " не найден");
    }
}