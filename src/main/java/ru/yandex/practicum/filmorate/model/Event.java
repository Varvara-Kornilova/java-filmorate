package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// Модель события в ленте
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Event {

    private Long eventId; // id события
    private Long timestamp; // время события
    private Long userId; // кто совершил действие
    private EventType eventType; // тип события
    private EventOperation operation; // тип операции
    private Long entityId; // id сущности (фильм, пользователь и тд)
}