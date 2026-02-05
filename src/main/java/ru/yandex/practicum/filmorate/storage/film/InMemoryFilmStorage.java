package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.*;

@Component
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Long, Film> films = new HashMap<>();

    @Override
    public Collection<Film> findAllFilms() {
        return films.values();
    }

    @Override
    public Film create(Film film) {
        film.setId(getNextId());
        films.put(film.getId(), film);
        //log.info("Создан фильм: {}", film);
        return film;
    }

    @Override
    public Film update(Film newFilm) {
        if (newFilm.getId() == null) {
            //log.warn("null ID");
            throw new ValidationException("ID фильма не может быть пустым при обновлении");
        }

        if (!films.containsKey(newFilm.getId())) {
            //log.warn("Введен несуществующий id фильма: {}", newFilm.getId());
            throw new NotFoundException("Фильм с id = " + newFilm.getId() + " не найден");
        }

        films.put(newFilm.getId(), newFilm);
        //log.info("Обновлены данные о фильме c id {}: {}", newFilm.getId(), newFilm.getName());
        return newFilm;
    }

    @Override
    public Optional<Film> findById(Long id) {
        if (id == null || id < 0) {
            return Optional.empty();
        }

        return Optional.ofNullable(films.get(id));
    }

    private long getNextId() {
        long currentMaxId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
