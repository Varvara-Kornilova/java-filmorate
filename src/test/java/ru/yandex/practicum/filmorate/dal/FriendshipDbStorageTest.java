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
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({FriendshipDbStorage.class, UserDbStorage.class, UserRowMapper.class})
@Sql(scripts = "/data.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
class FriendshipDbStorageTest {

    private final FriendshipDbStorage friendshipStorage;
    private final UserDbStorage userStorage;
    private final JdbcTemplate jdbcTemplate;

    private User user1;
    private User user2;
    private User user3;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("DELETE FROM friendship");
        jdbcTemplate.update("DELETE FROM users");

        user1 = createUser("u1@test.com", "user1", "User One");
        user2 = createUser("u2@test.com", "user2", "User Two");
        user3 = createUser("u3@test.com", "user3", "User Three");
    }

    @Test
    void testAddFriend() {
        friendshipStorage.addFriend(user1.getId(), user2.getId());

        Collection<User> friends = friendshipStorage.getFriends(user1.getId());
        assertThat(friends).extracting("id").contains(user2.getId());
    }

    @Test
    void testAddFriendAlreadyExists() {
        friendshipStorage.addFriend(user1.getId(), user2.getId());
        friendshipStorage.addFriend(user1.getId(), user2.getId()); // повторный вызов

        Collection<User> friends = friendshipStorage.getFriends(user1.getId());
        assertThat(friends).extracting("id").containsExactly(user2.getId());
    }

    @Test
    void testRemoveFriend() {
        friendshipStorage.addFriend(user1.getId(), user2.getId());
        friendshipStorage.removeFriend(user1.getId(), user2.getId());

        Collection<User> friends = friendshipStorage.getFriends(user1.getId());
        assertThat(friends).doesNotContain(user2);
    }

    @Test
    void testGetFriendsEmpty() {
        Collection<User> friends = friendshipStorage.getFriends(user1.getId());
        assertThat(friends).isEmpty();
    }

    @Test
    void testGetCommonFriends() {
        friendshipStorage.addFriend(user1.getId(), user3.getId());
        friendshipStorage.addFriend(user2.getId(), user3.getId());

        Collection<User> common = friendshipStorage.getCommonFriends(user1.getId(), user2.getId());

        assertThat(common).extracting("id").contains(user3.getId());
    }

    @Test
    void testGetCommonFriendsEmpty() {
        friendshipStorage.addFriend(user1.getId(), user3.getId());
        // user2 не имеет общих друзей с user1

        Collection<User> common = friendshipStorage.getCommonFriends(user1.getId(), user2.getId());
        assertThat(common).isEmpty();
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
