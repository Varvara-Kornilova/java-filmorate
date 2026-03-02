package ru.yandex.practicum.filmorate.service.mpa;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;

import java.util.Collection;

@Service
public class MpaService {

    private final MpaStorage mpaStorage;

    public MpaService(MpaStorage mpaStorage) {
        this.mpaStorage = mpaStorage;
    }

    public Collection<Mpa> getAllRatings() {
        return mpaStorage.findAll();
    }

    public Mpa getRatingById(Long id) {
        return mpaStorage.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        String.format("Рейтинг с идентификатором %d не существует", id)));
    }

    public boolean ratingExists(Long id) {
        return mpaStorage.findById(id).isPresent();
    }
}