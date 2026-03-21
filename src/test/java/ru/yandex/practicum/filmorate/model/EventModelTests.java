package ru.yandex.practicum.filmorate.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class EventModelTests {

    @Test
    public void testEventBuilderCreatesCorrectObject() {
        // создаем событие через builder
        Event event = Event.builder()
                .eventId(1L)
                .timestamp(123456789L)
                .userId(10L)
                .eventType(EventType.LIKE)
                .operation(EventOperation.ADD)
                .entityId(20L)
                .build();

        // проверяем, что все поля сохранились правильно
        assertThat(event.getEventId()).isEqualTo(1L);
        assertThat(event.getTimestamp()).isEqualTo(123456789L);
        assertThat(event.getUserId()).isEqualTo(10L);
        assertThat(event.getEventType()).isEqualTo(EventType.LIKE);
        assertThat(event.getOperation()).isEqualTo(EventOperation.ADD);
        assertThat(event.getEntityId()).isEqualTo(20L);
    }

    @Test
    public void testReviewEventBuilderCreatesCorrectObject() {
        // создаем событие отзыва через builder
        Event event = Event.builder()
                .eventId(2L)
                .timestamp(555555555L)
                .userId(30L)
                .eventType(EventType.REVIEW)
                .operation(EventOperation.UPDATE)
                .entityId(40L)
                .build();

        // проверяем, что review-событие сохранилось правильно
        assertThat(event.getEventId()).isEqualTo(2L);
        assertThat(event.getTimestamp()).isEqualTo(555555555L);
        assertThat(event.getUserId()).isEqualTo(30L);
        assertThat(event.getEventType()).isEqualTo(EventType.REVIEW);
        assertThat(event.getOperation()).isEqualTo(EventOperation.UPDATE);
        assertThat(event.getEntityId()).isEqualTo(40L);
    }

    @Test
    public void testEventTypeContainsExpectedValues() {
        // проверяем, что enum типов событий содержит нужные значения
        assertThat(EventType.valueOf("LIKE")).isEqualTo(EventType.LIKE);
        assertThat(EventType.valueOf("FRIEND")).isEqualTo(EventType.FRIEND);
        assertThat(EventType.valueOf("REVIEW")).isEqualTo(EventType.REVIEW);
    }

    @Test
    public void testEventOperationContainsExpectedValues() {
        // проверяем, что enum операций содержит нужные значения
        assertThat(EventOperation.valueOf("ADD")).isEqualTo(EventOperation.ADD);
        assertThat(EventOperation.valueOf("REMOVE")).isEqualTo(EventOperation.REMOVE);
        assertThat(EventOperation.valueOf("UPDATE")).isEqualTo(EventOperation.UPDATE);
    }

    @Test
    public void testNoArgsConstructorAndSetters() {
        // создаем пустой объект и заполняем его через сеттеры
        Event event = new Event();
        event.setEventId(3L);
        event.setTimestamp(987654321L);
        event.setUserId(15L);
        event.setEventType(EventType.FRIEND);
        event.setOperation(EventOperation.REMOVE);
        event.setEntityId(25L);

        // проверяем, что сеттеры отработали правильно
        assertThat(event.getEventId()).isEqualTo(3L);
        assertThat(event.getTimestamp()).isEqualTo(987654321L);
        assertThat(event.getUserId()).isEqualTo(15L);
        assertThat(event.getEventType()).isEqualTo(EventType.FRIEND);
        assertThat(event.getOperation()).isEqualTo(EventOperation.REMOVE);
        assertThat(event.getEntityId()).isEqualTo(25L);
    }
}