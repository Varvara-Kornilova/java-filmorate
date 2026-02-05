package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.Optional;

public interface FilmStorage {

    public Collection<Film> findAllFilms();

    public Film create(Film film);

    public Film update(Film newFilm);

    public Optional<Film> findById(Long id);
}
