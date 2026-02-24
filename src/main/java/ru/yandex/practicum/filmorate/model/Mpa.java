package ru.yandex.practicum.filmorate.model;

import lombok.Getter;

@Getter
public enum Mpa {
    G(1, "G", "у фильма нет возрастных ограничений"),
    PG(2, "PG", "детям рекомендуется смотреть фильм с родителями"),
    PG_13(3, "PG-13", "детям до 13 лет просмотр не желателен"),
    R(4, "R", "лицам до 17 лет просматривать фильм можно только в присутствии взрослого"),
    NC_17(5, "NC-17", "лицам до 18 лет просмотр запрещён");

    private final long id;
    private final String code;
    private final String description;

    Mpa(long id, String code, String description) {
        this.id = id;
        this.code = code;
        this.description = description;
    }

    public static Mpa getById(long id) {
        for (Mpa rating : values()) {
            if (rating.id == id) {
                return rating;
            }
        }
        throw new IllegalArgumentException("Рейтинг с id " + id + " не найден");
    }

    public static Mpa getByCode(String code) {
        for (Mpa rating : values()) {
            if (rating.code.equals(code)) {
                return rating;
            }
        }
        throw new IllegalArgumentException("Рейтинг с кодом " + code + " не найден");
    }
}
