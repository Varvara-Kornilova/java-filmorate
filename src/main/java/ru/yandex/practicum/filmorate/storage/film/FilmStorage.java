package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;
import java.util.Collection;
import java.util.Optional;

public interface FilmStorage {

    Collection<Film> findAll();

    Film create(Film film);

    Film update(Film film);

    Optional<Film> findById(Long id);

    Film delete(Long id);

    Collection<Film> getPopular(int count);

    Long addLike(Long filmId, Long userId);

    Long removeLike(Long filmId, Long userId);

    boolean contains(Long id);

    Collection<Film> findByDirectorId(Long directorId, String sortBy);
}
