package ru.yandex.practicum.filmorate.storage.genre;

import ru.yandex.practicum.filmorate.model.Genre;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;

public interface GenreStorage {

    Collection<Genre> findAll();

    Optional<Genre> findById(Long id);

    void setGenres(Long filmId, Set<Long> genreIds);

    void updateFilmGenres(Long filmId);

    Set<Genre> getGenresByFilmId(Long filmId);
}
