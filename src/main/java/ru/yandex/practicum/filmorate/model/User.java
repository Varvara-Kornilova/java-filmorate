package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * User.
 */
@Data
public class User {

    /** Целочисленный идентификатор. */
    private Long id;

    /** Электронная почта. */
    @Email(message = "Некорректный формат электронной почты")
    @NotBlank(message = "Электронная почта не может быть пустой")
    private String email;

    /** Логин пользователя. */
    @NotBlank(message = "Логин не может быть пустым")
    @Pattern(regexp = "^[^\\s]+$", message = "Логин не должен содержать пробелы")
    private String login;

    /** Имя для отображения. */
    private String name;

    /** Дата рождения. */
    @PastOrPresent(message = "Дата рождения не может быть в будущем")
    private LocalDate birthday;

    /** Список друзей пользователя (множество уникальных ID). */
    private Set<Long> friends = new HashSet<>();
}
