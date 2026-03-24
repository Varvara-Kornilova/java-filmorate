package ru.yandex.practicum.filmorate.service.mpa;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;

import java.util.Collection;

@Slf4j
@Service
@RequiredArgsConstructor
public class MpaService {

    private final MpaStorage mpaStorage;

    public Collection<Mpa> getAllRatings() {
        log.debug("Запрошен список всех рейтингов MPA");
        Collection<Mpa> ratings = mpaStorage.findAll();
        log.debug("Найдено {} рейтингов MPA", ratings.size());
        return ratings;
    }

    public Mpa getRatingById(Long id) {
        log.debug("Поиск рейтинга MPA по id={}", id);
        return mpaStorage.findById(id)
                .orElseThrow(() -> {
                    log.warn("Рейтинг MPA с id={} не найден", id);
                    return new NotFoundException(
                            String.format("Рейтинг с идентификатором %d не существует", id));
                });
    }

    public boolean ratingExists(Long id) {
        log.debug("Проверка существования рейтинга MPA с id={}", id);
        boolean exists = mpaStorage.findById(id).isPresent();
        log.debug("Рейтинг MPA с id={} {}существует", id, exists ? "" : "не ");
        return exists;
    }
}