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
        jdbcTemplate.update("DELETE FROM events");
        jdbcTemplate.update("DELETE FROM users");
    }

    @Test
    public void testCreateEvent() {
        Long userId = createTestUser("test@test.com", "test");

        Event event = Event.builder()
                .timestamp(System.currentTimeMillis())
                .userId(userId)
                .eventType(EventType.LIKE)
                .operation(EventOperation.ADD)
                .entityId(1L)
                .build();

        Event created = eventStorage.create(event);

        assertThat(created.getEventId()).isNotNull();

        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM events WHERE user_id = ?",
                Integer.class,
                userId
        );

        assertThat(count).isEqualTo(1);
    }

    @Test
    public void testGetUserFeed() {
        Long userId = createTestUser("feed@test.com", "feed");

        eventStorage.create(createEvent(userId, EventType.LIKE, EventOperation.ADD, 1L));
        eventStorage.create(createEvent(userId, EventType.FRIEND, EventOperation.ADD, 2L));

        List<Event> feed = eventStorage.getUserFeed(userId);

        assertThat(feed).hasSize(2);

        assertThat(feed).extracting("eventType")
                .containsExactly(EventType.LIKE, EventType.FRIEND);
    }

    @Test
    public void testFeedSortedByTimestamp() throws InterruptedException {
        Long userId = createTestUser("sort@test.com", "sort");

        Event first = eventStorage.create(createEvent(userId, EventType.LIKE, EventOperation.ADD, 1L));

        Thread.sleep(5);

        Event second = eventStorage.create(createEvent(userId, EventType.FRIEND, EventOperation.ADD, 2L));

        List<Event> feed = eventStorage.getUserFeed(userId);

        assertThat(feed).hasSize(2);
        assertThat(feed.get(0).getEventId()).isEqualTo(first.getEventId());
        assertThat(feed.get(1).getEventId()).isEqualTo(second.getEventId());
    }

    @Test
    public void testEmptyFeed() {
        Long userId = createTestUser("empty@test.com", "empty");

        List<Event> feed = eventStorage.getUserFeed(userId);

        assertThat(feed).isEmpty();
    }

    @Test
    public void testCreateReviewEvent() {
        Long userId = createTestUser("review@test.com", "review");

        Event event = createEvent(userId, EventType.REVIEW, EventOperation.ADD, 10L);

        Event created = eventStorage.create(event);

        List<Event> feed = eventStorage.getUserFeed(userId);

        assertThat(created.getEventId()).isNotNull();
        assertThat(feed).hasSize(1);
        assertThat(feed.getFirst().getEventType()).isEqualTo(EventType.REVIEW);
        assertThat(feed.getFirst().getOperation()).isEqualTo(EventOperation.ADD);
        assertThat(feed.getFirst().getEntityId()).isEqualTo(10L);
    }

    @Test
    public void testGetUserFeedReturnsOnlyCurrentUserEvents() {
        Long firstUserId = createTestUser("first@test.com", "first");
        Long secondUserId = createTestUser("second@test.com", "second");

        eventStorage.create(createEvent(firstUserId, EventType.LIKE, EventOperation.ADD, 1L));
        eventStorage.create(createEvent(secondUserId, EventType.FRIEND, EventOperation.ADD, 2L));

        List<Event> firstUserFeed = eventStorage.getUserFeed(firstUserId);

        assertThat(firstUserFeed).hasSize(1);
        assertThat(firstUserFeed.getFirst().getUserId()).isEqualTo(firstUserId);
        assertThat(firstUserFeed.getFirst().getEventType()).isEqualTo(EventType.LIKE);
    }

    private Event createEvent(Long userId, EventType type, EventOperation operation, Long entityId) {
        return Event.builder()
                .timestamp(System.currentTimeMillis())
                .userId(userId)
                .eventType(type)
                .operation(operation)
                .entityId(entityId)
                .build();
    }

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