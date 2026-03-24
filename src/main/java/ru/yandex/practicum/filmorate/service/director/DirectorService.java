package ru.yandex.practicum.filmorate.service.director;

import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class DirectorService {

    private final DirectorStorage directorStorage;

    public Collection<Director> getAllDirectors() {
        log.debug("Запрошен список всех режиссёров");
        Collection<Director> result = directorStorage.findAll();
        log.debug("Найдено {} режиссёров", result.size());
        return result;
    }

    public Director getDirectorById(Long id) {
        log.debug("Запрошен режиссёр с id={}", id);
        return directorStorage.findById(id)
                .orElseThrow(() -> {
                    log.warn("Режиссёр с id={} не найден", id);
                    return new NotFoundException(
                            String.format("Режиссёр с идентификатором %d не найден", id));
                });
    }

    public Director createDirector(Director director) {
        log.debug("Создание режиссёра: name='{}'", director.getName());

        if (director.getName() == null || director.getName().isBlank()) {
            log.warn("Попытка создания режиссёра с пустым именем");
            throw new ValidationException("Имя режиссёра не может быть пустым");
        }

        Director created = directorStorage.create(director);
        log.info("Режиссёр создан: id={}, name='{}'", created.getId(), created.getName());
        return created;
    }

    public Director updateDirector(Director director) {
        log.debug("Обновление режиссёра: id={}", director.getId());

        if (director.getId() == null) {
            log.warn("Попытка обновления режиссёра без указания ID");
            throw new ValidationException("ID режиссёра должен быть указан");
        }

        if (!directorStorage.exists(director.getId())) {
            log.warn("Режиссёр с id={} не найден при обновлении", director.getId());
            throw new NotFoundException(
                    String.format("Режиссёр с идентификатором %d не найден", director.getId()));
        }

        if (director.getName() == null || director.getName().isBlank()) {
            log.warn("Попытка обновления режиссёра id={} с пустым именем", director.getId());
            throw new ValidationException("Имя режиссёра не может быть пустым");
        }

        Director updated = directorStorage.update(director);
        log.info("Режиссёр обновлён: id={}, name='{}'", updated.getId(), updated.getName());
        return updated;
    }

    public void deleteDirector(Long id) {
        log.debug("Удаление режиссёра с id={}", id);

        if (!directorStorage.exists(id)) {
            log.warn("Попытка удаления несуществующего режиссёра с id={}", id);
            throw new NotFoundException(
                    String.format("Режиссёр с идентификатором %d не найден", id));
        }

        directorStorage.delete(id);
        log.info("Режиссёр с id={} успешно удалён", id);
    }

    public void validateDirectors(Set<Long> directorIds) {

        if (directorIds == null || directorIds.isEmpty()) {
            log.debug("Список ID режиссёров пуст, валидация пропущена");
            return;
        }

        log.debug("Начата валидация {} режиссёров", directorIds.size());

        for (Long directorId : directorIds) {
            directorStorage.findById(directorId)
                    .orElseThrow(() -> {
                            log.warn("При валидации не найден режиссёр с id={}", directorId);
                    return new NotFoundException(
                            String.format("Режиссёр с идентификатором %d не существует", directorId));
                    });
        }

        log.debug("Валидация режиссёров успешно завершена");
    }
}