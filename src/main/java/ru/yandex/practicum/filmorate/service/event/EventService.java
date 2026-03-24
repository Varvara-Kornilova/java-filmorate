package ru.yandex.practicum.filmorate.service.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.EventOperation;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.storage.event.EventStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class EventService {

    private final EventStorage eventStorage;
    private final UserStorage userStorage;

    public List<Event> getUserFeed(Long userId) {
        log.debug("Запрос ленты событий для пользователя id={}", userId);
        validateUserExists(userId);

        List<Event> feed = eventStorage.getUserFeed(userId);
        log.debug("В ленте пользователя {} найдено {} событий", userId, feed.size());
        return feed;
    }

    @Transactional
    public Event addEvent(Long userId, EventType eventType, EventOperation operation, Long entityId) {
        log.debug("Создание события: userId={}, type={}, operation={}, entityId={}",
                userId, eventType, operation, entityId);

        validateUserExists(userId);

        Event event = Event.builder()
                .timestamp(System.currentTimeMillis())
                .userId(userId)
                .eventType(eventType)
                .operation(operation)
                .entityId(entityId)
                .build();

        Event created = eventStorage.create(event);
        log.info("Событие создано: userId={}, type={}, operation={}, entityId={}",
                userId, eventType, operation, entityId);
        return created;
    }

    private void validateUserExists(Long userId) {
        log.debug("Проверка существования пользователя с id={}", userId);
        userStorage.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Пользователь с id={} не найден при работе с событиями", userId);
                    return new NotFoundException(
                            String.format("Пользователь с идентификатором %d не найден", userId));
                });
    }
}