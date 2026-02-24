package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Friendship {
    /** ID пользователя, который отправил запрос */
    private Long userId;

    /** ID пользователя, которому отправлен запрос */
    private Long friendId;

    /** Статус дружбы */
    private FriendshipStatus status;

    /**
     * Подтвердить дружбу
     */
    public void confirm() {
        this.status = FriendshipStatus.CONFIRMED;
    }

    /**
     * Проверить, подтверждена ли дружба
     */
    public boolean isConfirmed() {
        return this.status == FriendshipStatus.CONFIRMED;
    }
}
