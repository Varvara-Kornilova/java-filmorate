package ru.yandex.practicum.filmorate.service.event;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.EventOperation;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.storage.event.EventStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;

// Сервис для работы с лентой событий
@Service
@Transactional(readOnly = true)
public class EventService {

    private final EventStorage eventStorage;
    private final UserStorage userStorage;

    public EventService(EventStorage eventStorage, UserStorage userStorage) {
        this.eventStorage = eventStorage;
        this.userStorage = userStorage;
    }

    // Получаем ленту событий пользователя
    public List<Event> getUserFeed(Long userId) {
        validateUserExists(userId);
        return eventStorage.getUserFeed(userId);
    }

    // Сохраняем новое событие
    @Transactional
    public Event addEvent(Long userId, EventType eventType, EventOperation operation, Long entityId) {
        validateUserExists(userId);

        Event event = Event.builder()
                .timestamp(System.currentTimeMillis())
                .userId(userId)
                .eventType(eventType)
                .operation(operation)
                .entityId(entityId)
                .build();

        return eventStorage.create(event);
    }

    // Проверяем, что пользователь существует
    private void validateUserExists(Long userId) {
        userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException(
                        String.format("Пользователь с идентификатором %d не найден", userId)));
    }
}