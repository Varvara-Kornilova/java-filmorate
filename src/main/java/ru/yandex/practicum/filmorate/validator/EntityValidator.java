package ru.yandex.practicum.filmorate.validator;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

@Component
public class EntityValidator {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    public EntityValidator(FilmStorage filmStorage, UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public Film getFilmOrThrow(Long filmId) {
        if (filmId == null || filmId <= 0) {
            throw new ValidationException("ID фильма не может быть пустым или отрицательным");
        }

        return filmStorage.findById(filmId)
                .orElseThrow(() -> new NotFoundException("Фильм с id = " + filmId + " не найден"));
    }

    public User getUserOrThrow(Long userId) {
        if (userId == null || userId <= 0) {
            throw new ValidationException("ID пользователя не может быть пустым или отрицательным");
        }

        return userStorage.findUserById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + userId + " не найден"));

    }
}
