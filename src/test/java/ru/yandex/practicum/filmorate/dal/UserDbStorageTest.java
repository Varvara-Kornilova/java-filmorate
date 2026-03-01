package ru.yandex.practicum.filmorate.dal;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import(UserDbStorage.class)
class UserDbStorageTest {

    private final UserDbStorage userStorage;

    @Test
    void shouldCreateUser() {
        User user = new User();
        user.setEmail("create@example.com");
        user.setLogin("createuser");
        user.setName("Create Test");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        User created = userStorage.create(user);

        assertThat(created.getId()).isNotNull();
        assertThat(created.getId()).isPositive();
        assertThat(created.getEmail()).isEqualTo("create@example.com");
        assertThat(created.getLogin()).isEqualTo("createuser");
        assertThat(created.getName()).isEqualTo("Create Test");
    }

    @Test
    void shouldUpdateUser() {
        User user = new User();
        user.setEmail("update@example.com");
        user.setLogin("updateuser");
        user.setName("Before");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        User created = userStorage.create(user);

        created.setName("After Update");
        created.setEmail("new@example.com");
        User updated = userStorage.update(created);

        assertThat(updated.getName()).isEqualTo("After Update");
        assertThat(updated.getEmail()).isEqualTo("new@example.com");
        assertThat(updated.getId()).isEqualTo(created.getId());
    }

    @Test
    void shouldFindUserById_whenExists() {
        User user = new User();
        user.setEmail("find@example.com");
        user.setLogin("finduser");
        user.setName("Find Me");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        User created = userStorage.create(user);

        Optional<User> found = userStorage.findUserById(created.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(created.getId());
        assertThat(found.get().getLogin()).isEqualTo("finduser");
    }

    @Test
    void shouldReturnEmpty_whenUserNotFound() {
        Optional<User> found = userStorage.findUserById(999L);

        assertThat(found).isEmpty();
    }

    @Test
    void shouldFindAllUsers() {
        User user1 = new User();
        user1.setEmail("user1@example.com");
        user1.setLogin("user1");
        user1.setName("User One");
        user1.setBirthday(LocalDate.of(1990, 1, 1));

        User user2 = new User();
        user2.setEmail("user2@example.com");
        user2.setLogin("user2");
        user2.setName("User Two");
        user2.setBirthday(LocalDate.of(1991, 2, 2));

        userStorage.create(user1);
        userStorage.create(user2);

        List<User> users = userStorage.findAllUsers();

        assertThat(users).hasSize(2);
        assertThat(users).extracting("login").containsExactlyInAnyOrder("user1", "user2");
    }

    @Test
    void shouldReturnEmptyList_whenNoUsers() {
        List<User> users = userStorage.findAllUsers();

        assertThat(users).isEmpty();
    }
}
