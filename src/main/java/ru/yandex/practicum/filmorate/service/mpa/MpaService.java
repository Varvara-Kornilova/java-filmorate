package ru.yandex.practicum.filmorate.service.mpa;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;

import java.util.Collection;

/**
 * Сервис для работы с возрастными рейтингами (MPA).
 */
@Service
public class MpaService {

    private final MpaStorage mpaStorage;

    public MpaService(MpaStorage mpaStorage) {
        this.mpaStorage = mpaStorage;
    }

    /**
     * Возвращает список всех доступных рейтингов.
     */
    public Collection<Mpa> getAllRatings() {
        return mpaStorage.findAll();
    }

    /**
     * Находит рейтинг по идентификатору.
     * @throws NotFoundException если рейтинг не найден
     */
    public Mpa getRatingById(Long id) {
        return mpaStorage.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        String.format("Рейтинг с идентификатором %d не существует", id)));
    }

    /**
     * Проверяет существование рейтинга в базе.
     */
    public boolean ratingExists(Long id) {
        return mpaStorage.findById(id).isPresent();
    }
}