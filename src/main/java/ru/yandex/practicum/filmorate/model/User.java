package ru.yandex.practicum.filmorate.model;

import lombok.Data;

import java.time.LocalDate;

/**
 * User.
 */
@Data
public class User {

    /** Целочисленный идентификатор. */
    private Long id;

    /** Электронная почта. */
    private String email;

    /** Логин пользователя. */
    private String login;

    /** Имя для отображения. */
    private String name;

    /** Дата рождения. */
    private LocalDate birthday;
}
