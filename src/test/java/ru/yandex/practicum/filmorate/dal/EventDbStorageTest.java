package ru.yandex.practicum.filmorate.dal;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import ru.yandex.practicum.filmorate.dal.mappers.EventRowMapper;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.EventOperation;
import ru.yandex.practicum.filmorate.model.EventType;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({
        EventDbStorage.class,
        EventRowMapper.class
})
@Sql(scripts = "/data.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
public class EventDbStorageTest {

    private final EventDbStorage eventStorage;
    private final JdbcTemplate jdbcTemplate;

    @BeforeEach
    public void cleanUp() {
        // очищаем таблицы перед каждым тестом
        jdbcTemplate.update("DELETE FROM events");
        jdbcTemplate.update("DELETE FROM users");
    }

    @Test
    public void testCreateEvent() {
        // создаем пользователя
        Long userId = createTestUser("test@test.com", "test");

        // создаем событие
        Event event = Event.builder()
                .timestamp(System.currentTimeMillis())
                .userId(userId)
                .eventType(EventType.LIKE)
                .operation(EventOperation.ADD)
                .entityId(1L)
                .build();

        // сохраняем событие
        Event created = eventStorage.create(event);

        // проверяем, что id события появился
        assertThat(created.getEventId()).isNotNull();

        // проверяем, что запись действительно появилась в базе
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM events WHERE user_id = ?",
                Integer.class,
                userId
        );

        assertThat(count).isEqualTo(1);
    }

    @Test
    public void testGetUserFeed() {
        // создаем пользователя
        Long userId = createTestUser("feed@test.com", "feed");

        // добавляем несколько событий
        eventStorage.create(createEvent(userId, EventType.LIKE, EventOperation.ADD, 1L));
        eventStorage.create(createEvent(userId, EventType.FRIEND, EventOperation.ADD, 2L));

        // получаем ленту
        List<Event> feed = eventStorage.getUserFeed(userId);

        // проверяем количество событий
        assertThat(feed).hasSize(2);

        // проверяем, что события пришли в правильном порядке
        assertThat(feed).extracting("eventType")
                .containsExactly(EventType.LIKE, EventType.FRIEND);
    }

    @Test
    public void testFeedSortedByTimestamp() throws InterruptedException {
        // создаем пользователя
        Long userId = createTestUser("sort@test.com", "sort");

        // создаем первое событие
        Event first = eventStorage.create(createEvent(userId, EventType.LIKE, EventOperation.ADD, 1L));

        // делаем небольшую паузу, чтобы timestamp отличался
        Thread.sleep(5);

        // создаем второе событие
        Event second = eventStorage.create(createEvent(userId, EventType.FRIEND, EventOperation.ADD, 2L));

        // получаем ленту
        List<Event> feed = eventStorage.getUserFeed(userId);

        // проверяем порядок событий по времени
        assertThat(feed).hasSize(2);
        assertThat(feed.get(0).getEventId()).isEqualTo(first.getEventId());
        assertThat(feed.get(1).getEventId()).isEqualTo(second.getEventId());
    }

    @Test
    public void testEmptyFeed() {
        // создаем пользователя без событий
        Long userId = createTestUser("empty@test.com", "empty");

        // получаем ленту
        List<Event> feed = eventStorage.getUserFeed(userId);

        // проверяем, что лента пустая
        assertThat(feed).isEmpty();
    }

    @Test
    public void testCreateReviewEvent() {
        // создаем пользователя
        Long userId = createTestUser("review@test.com", "review");

        // создаем событие отзыва
        Event event = createEvent(userId, EventType.REVIEW, EventOperation.ADD, 10L);

        // сохраняем событие
        Event created = eventStorage.create(event);

        // получаем ленту пользователя
        List<Event> feed = eventStorage.getUserFeed(userId);

        // проверяем, что review-событие сохранилось корректно
        assertThat(created.getEventId()).isNotNull();
        assertThat(feed).hasSize(1);
        assertThat(feed.getFirst().getEventType()).isEqualTo(EventType.REVIEW);
        assertThat(feed.getFirst().getOperation()).isEqualTo(EventOperation.ADD);
        assertThat(feed.getFirst().getEntityId()).isEqualTo(10L);
    }

    @Test
    public void testGetUserFeedReturnsOnlyCurrentUserEvents() {
        // создаем двух пользователей
        Long firstUserId = createTestUser("first@test.com", "first");
        Long secondUserId = createTestUser("second@test.com", "second");

        // добавляем события двум разным пользователям
        eventStorage.create(createEvent(firstUserId, EventType.LIKE, EventOperation.ADD, 1L));
        eventStorage.create(createEvent(secondUserId, EventType.FRIEND, EventOperation.ADD, 2L));

        // получаем ленту первого пользователя
        List<Event> firstUserFeed = eventStorage.getUserFeed(firstUserId);

        // проверяем, что в ленте только его события
        assertThat(firstUserFeed).hasSize(1);
        assertThat(firstUserFeed.getFirst().getUserId()).isEqualTo(firstUserId);
        assertThat(firstUserFeed.getFirst().getEventType()).isEqualTo(EventType.LIKE);
    }

    // создаем тестовое событие
    private Event createEvent(Long userId, EventType type, EventOperation operation, Long entityId) {
        return Event.builder()
                .timestamp(System.currentTimeMillis())
                .userId(userId)
                .eventType(type)
                .operation(operation)
                .entityId(entityId)
                .build();
    }

    // создаем тестового пользователя напрямую в базе
    private Long createTestUser(String email, String login) {
        jdbcTemplate.update(
                "INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?)",
                email, login, login, "1990-01-01"
        );

        return jdbcTemplate.queryForObject(
                "SELECT user_id FROM users WHERE login = ?",
                Long.class,
                login
        );
    }
}