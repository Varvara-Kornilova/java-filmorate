package ru.yandex.practicum.filmorate.service.genre;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;

import java.util.Collection;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class GenreService {

    private final GenreStorage genreStorage;

    public Collection<Genre> getAllGenres() {
        log.debug("Запрошен список всех жанров");
        Collection<Genre> genres = genreStorage.findAll();
        log.debug("Найдено {} жанров", genres.size());
        return genres;
    }

    public Genre getGenreById(Long id) {
        log.debug("Поиск жанра по id={}", id);
        return genreStorage.findById(id)
                .orElseThrow(() -> {
                    log.warn("Жанр с id={} не найден", id);
                    return new NotFoundException(
                            String.format("Жанр с идентификатором %d не найден в системе", id));
                });
    }

    public void validateGenres(Set<Long> genreIds) {
        if (genreIds == null || genreIds.isEmpty()) {
            log.debug("Список ID жанров пуст, валидация пропущена");
            return;
        }

        log.debug("Начата валидация {} жанров", genreIds.size());

        for (Long genreId : genreIds) {
            genreStorage.findById(genreId)
                    .orElseThrow(() -> {
                        log.warn("При валидации не найден жанр с id={}", genreId);
                        return new NotFoundException(
                                String.format("Жанр с идентификатором %d не существует", genreId));
                    });
        }

        log.debug("Валидация жанров успешно завершена");
    }
}