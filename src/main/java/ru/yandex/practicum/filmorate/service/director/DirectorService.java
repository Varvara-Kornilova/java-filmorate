package ru.yandex.practicum.filmorate.service.director;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.director.DirectorStorage;

import java.util.Collection;
import java.util.Set;

@Slf4j
@Service
@Transactional
public class DirectorService {

    private final DirectorStorage directorStorage;

    public DirectorService(DirectorStorage directorStorage) {
        this.directorStorage = directorStorage;
    }

    public Collection<Director> getAllDirectors() {
        return directorStorage.findAll();
    }

    public Director getDirectorById(Long id) {
        return directorStorage.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        String.format("Режиссёр с идентификатором %d не найден", id)));
    }

    public Director createDirector(Director director) {
        if (director.getName() == null || director.getName().isBlank()) {
            throw new ValidationException("Имя режиссёра не может быть пустым");
        }
        return directorStorage.create(director);
    }

    public Director updateDirector(Director director) {
        if (director.getId() == null) {
            throw new ValidationException("ID режиссёра должен быть указан");
        }
        if (!directorStorage.exists(director.getId())) {
            throw new NotFoundException(
                    String.format("Режиссёр с идентификатором %d не найден", director.getId()));
        }
        if (director.getName() == null || director.getName().isBlank()) {
            throw new ValidationException("Имя режиссёра не может быть пустым");
        }
        return directorStorage.update(director);
    }

    public void deleteDirector(Long id) {
        if (!directorStorage.exists(id)) {
            throw new NotFoundException(
                    String.format("Режиссёр с идентификатором %d не найден", id));
        }
        directorStorage.delete(id);
        log.info("Режиссёр с идентификатором {} удалён", id);
    }

    public void validateDirectors(Set<Long> directorIds) {
        if (directorIds == null || directorIds.isEmpty()) return;

        for (Long directorId : directorIds) {
            directorStorage.findById(directorId)
                    .orElseThrow(() -> new NotFoundException(
                            String.format("Режиссёр с идентификатором %d не существует", directorId)));
        }
    }
}
