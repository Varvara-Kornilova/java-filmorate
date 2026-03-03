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
import ru.yandex.practicum.filmorate.dal.mappers.UserRowMapper;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({UserDbStorage.class, UserRowMapper.class})
@Sql(scripts = "/data.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
public class UserDbStorageTest {

    private final UserDbStorage userStorage;
    private final JdbcTemplate jdbcTemplate;

    @BeforeEach
    public void cleanUp() {
        jdbcTemplate.update("DELETE FROM friendship");
        jdbcTemplate.update("DELETE FROM users");
    }

    @Test
    public void testCreateAndFindById() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("testuser");
        user.setName("Test User");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        User created = userStorage.create(user);

        assertThat(created.getId()).isNotNull();

        Optional<User> found = userStorage.findById(created.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("test@example.com");
        assertThat(found.get().getLogin()).isEqualTo("testuser");
        assertThat(found.get().getName()).isEqualTo("Test User");
        assertThat(found.get().getBirthday()).isEqualTo(LocalDate.of(1990, 1, 1));
    }

    @Test
    public void testUpdate() {
        User user = createUser("old@example.com", "oldlogin", "Old Name");

        user.setName("Updated Name");
        user.setEmail("new@example.com");

        User updated = userStorage.update(user);

        Optional<User> found = userStorage.findById(user.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Updated Name");
        assertThat(found.get().getEmail()).isEqualTo("new@example.com");
    }

    @Test
    public void testFindAll() {
        createUser("u1@test.com", "user1", "User One");
        createUser("u2@test.com", "user2", "User Two");

        Collection<User> users = userStorage.findAll();

        assertThat(users).hasSize(2);
        assertThat(users).extracting("login").containsExactlyInAnyOrder("user1", "user2");
    }

    @Test
    public void testDelete() {
        User created = createUser("todelete@example.com", "todelete", "ToDelete");

        userStorage.delete(created.getId());

        Optional<User> found = userStorage.findById(created.getId());
        assertThat(found).isEmpty();
    }

    @Test
    public void testContains() {
        User created = createUser("exists@example.com", "exists", "Exists");

        assertThat(userStorage.contains(created.getId())).isTrue();
        assertThat(userStorage.contains(999L)).isFalse();
    }

    private User createUser(String email, String login, String name) {
        User user = new User();
        user.setEmail(email);
        user.setLogin(login);
        user.setName(name);
        user.setBirthday(LocalDate.of(1990, 1, 1));
        return userStorage.create(user);
    }
}