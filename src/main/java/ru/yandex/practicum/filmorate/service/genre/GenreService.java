package ru.yandex.practicum.filmorate.service.genre;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;

import java.util.Collection;
import java.util.Set;

/**
 * Сервис для управления жанрами фильмов.
 */
@Service
public class GenreService {

    private final GenreStorage genreStorage;

    public GenreService(GenreStorage genreStorage) {
        this.genreStorage = genreStorage;
    }

    /**
     * Возвращает все доступные жанры.
     */
    public Collection<Genre> getAllGenres() {
        return genreStorage.findAll();
    }

    /**
     * Находит жанр по идентификатору.
     * @throws NotFoundException если жанр не найден
     */
    public Genre getGenreById(Long id) {
        return genreStorage.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        String.format("Жанр с идентификатором %d не найден в системе", id)));
    }

    /**
     * Проверяет существование набора жанров.
     * @throws NotFoundException если хотя бы один жанр не найден
     */
    public void validateGenres(Set<Long> genreIds) {
        if (genreIds == null || genreIds.isEmpty()) {
            return;
        }
        for (Long genreId : genreIds) {
            genreStorage.findById(genreId)
                    .orElseThrow(() -> new NotFoundException(
                            String.format("Жанр с идентификатором %d не существует", genreId)));
        }
    }
}