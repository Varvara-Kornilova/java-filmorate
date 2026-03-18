package ru.yandex.practicum.filmorate.storage.director;

import ru.yandex.practicum.filmorate.model.Director;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;

public interface DirectorStorage {
    Collection<Director> findAll();
    Optional<Director> findById(Long id);
    Director create(Director director);
    Director update(Director director);
    void delete(Long id);
    boolean exists(Long id);

    void setDirectors(Long filmId, Set<Long> directorIds);
    void updateFilmDirectors(Long filmId);
    Set<Director> getDirectorsByFilmId(Long filmId);
}
