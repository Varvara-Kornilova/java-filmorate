package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;
import java.util.Collection;
import java.util.Optional;

public interface UserStorage {
    Collection<User> findAll();

    User create(User user);

    User update(User user);

    // Optional оставляем для безопасности, проверку делаем в Service
    Optional<User> findById(Long id);

    // Можно добавить удаление, если потребуется в будущем
    void delete(Long id);
}