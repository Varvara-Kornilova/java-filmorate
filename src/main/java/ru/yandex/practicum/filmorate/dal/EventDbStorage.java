package ru.yandex.practicum.filmorate.dal;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mappers.EventRowMapper;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.storage.event.EventStorage;

import java.util.List;

@Repository
public class EventDbStorage extends BaseDbStorage<Event> implements EventStorage {

    private static final String INSERT_EVENT = """
            INSERT INTO events (timestamp, user_id, event_type, operation, entity_id)
            VALUES (?, ?, ?, ?, ?)
            """;

    private static final String GET_USER_FEED = """
            SELECT
                event_id AS event_id,
                timestamp AS event_timestamp,
                user_id AS user_id,
                event_type AS event_type,
                operation AS event_operation,
                entity_id AS entity_id
            FROM events
            WHERE user_id = ?
            ORDER BY event_id ASC
            """;

    public EventDbStorage(JdbcTemplate jdbcTemplate, EventRowMapper eventRowMapper) {
        super(jdbcTemplate, eventRowMapper);
    }

    @Override
    public Event create(Event event) {
        Long eventId = insertAndGetId(
                INSERT_EVENT,
                event.getTimestamp(),
                event.getUserId(),
                event.getEventType().name(),
                event.getOperation().name(),
                event.getEntityId()
        );

        event.setEventId(eventId);
        return event;
    }

    @Override
    public List<Event> getUserFeed(Long userId) {
        return queryForList(GET_USER_FEED, userId);
    }
}