package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.Data;
import ru.yandex.practicum.filmorate.validator.ValidReleaseDate;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Data
public class Film {

    /** Целочисленный идентификатор. */
    private Long id;

    /** Название. */
    @NotBlank(message = "Название фильма не может быть пустым")
    private String name;

    /** Описание. */
    @Size(max = 200, message = "Максимальная длина описания — 200 символов")
    private String description;

    /** Дата релиза. */
    @ValidReleaseDate
    @PastOrPresent(message = "Дата релиза не может быть в будущем")
    private LocalDate releaseDate;

    /** Продолжительность фильма. */
    @Positive(message = "Продолжительность фильма должна быть положительным числом")
    private Integer duration;

    /** Лайки у фильма. */
    private Set<Long> likes = new HashSet<>();

    /** Жанры фильма (список ID) — для входящих запросов. */
    private Set<Long> genreIds = new HashSet<>();

    /** ID возрастного рейтинга — для входящих запросов. */
    private Long mpaRatingId;

    /** Объект рейтинга — для исходящих ответов. */
    private Mpa mpa;

    /** Жанры как объекты — для исходящих ответов. */
    private Set<Genre> genres = new HashSet<>();

    public void setMpa(Mpa mpa) {
        this.mpa = mpa;
        if (mpa != null && mpa.getId() != null) {
            this.mpaRatingId = mpa.getId();
        }
    }

    public void setGenres(Set<Genre> genres) {
        this.genres = genres;
        if (genres != null && !genres.isEmpty()) {
            this.genreIds = genres.stream()
                    .map(Genre::getId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
        }
    }
}
