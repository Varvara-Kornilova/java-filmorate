package ru.yandex.practicum.filmorate.dal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

public class FriendshipDbStorageTest extends BaseJdbcTest {

    private User user1;
    private User user2;
    private User user3;

    @BeforeEach
    public void setUp() {
        super.cleanUp();
        jdbcTemplate.update("DELETE FROM friendship");
        jdbcTemplate.update("DELETE FROM users");

        user1 = createUser("u1@test.com", "user1", "User One");
        user2 = createUser("u2@test.com", "user2", "User Two");
        user3 = createUser("u3@test.com", "user3", "User Three");
    }

    @Test
    public void testAddFriend() {
        friendshipStorage.addFriend(user1.getId(), user2.getId());
        Collection<User> friends = friendshipStorage.getFriends(user1.getId());
        assertThat(friends).extracting("id").contains(user2.getId());
    }

    @Test
    public void testAddFriendAlreadyExists() {
        friendshipStorage.addFriend(user1.getId(), user2.getId());
        friendshipStorage.addFriend(user1.getId(), user2.getId());

        Collection<User> friends = friendshipStorage.getFriends(user1.getId());
        assertThat(friends).extracting("id").containsExactly(user2.getId());
    }

    @Test
    public void testRemoveFriend() {
        friendshipStorage.addFriend(user1.getId(), user2.getId());
        friendshipStorage.removeFriend(user1.getId(), user2.getId());

        Collection<User> friends = friendshipStorage.getFriends(user1.getId());
        assertThat(friends).doesNotContain(user2);
    }

    @Test
    public void testGetFriendsEmpty() {
        Collection<User> friends = friendshipStorage.getFriends(user1.getId());
        assertThat(friends).isEmpty();
    }

    @Test
    public void testGetCommonFriends() {
        friendshipStorage.addFriend(user1.getId(), user3.getId());
        friendshipStorage.addFriend(user2.getId(), user3.getId());

        Collection<User> common = friendshipStorage.getCommonFriends(user1.getId(), user2.getId());
        assertThat(common).extracting("id").contains(user3.getId());
    }

    @Test
    public void testGetCommonFriendsEmpty() {
        friendshipStorage.addFriend(user1.getId(), user3.getId());
        Collection<User> common = friendshipStorage.getCommonFriends(user1.getId(), user2.getId());
        assertThat(common).isEmpty();
    }

    @Test
    public void testAddSelfAsFriend() {
        assertThatThrownBy(() -> friendshipStorage.addFriend(user1.getId(), user1.getId()))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Пользователь не может быть другом сам себе");

        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM friendship WHERE user_id = ? AND friend_id = ?",
                Integer.class, user1.getId(), user1.getId());
        assertThat(count).isEqualTo(0);
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