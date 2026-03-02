package ru.yandex.practicum.filmorate.dal.mappers;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashSet;

/**
 * Маппер для преобразования ResultSet в объект Film.
 * Обрабатывает базовые поля фильма и рейтинг (MPA).
 * Жанры загружаются отдельно через GenreStorage для упрощения логики маппинга.
 */
@Component
public class FilmRowMapper implements RowMapper<Film> {

    @Override
    public Film mapRow(ResultSet rs, int rowNum) throws SQLException {
        Film film = new Film();

        // === Базовые поля фильма ===
        film.setId(rs.getLong("film_id"));
        film.setName(rs.getString("name"));
        film.setDescription(rs.getString("description"));
        film.setDuration(rs.getInt("duration"));

        LocalDate releaseDate = rs.getObject("release_date", LocalDate.class);
        film.setReleaseDate(releaseDate);

        // === Маппинг рейтинга (MPA) ===
        extractMpaRating(film, rs);

        // === Инициализация пустого набора жанров ===
        // Жанры будут заполнены отдельно в FilmStorage через вызов getGenresByFilmId()
        film.setGenres(new HashSet<>());

        return film;
    }

    /**
     * Вынесена логика извлечения MPA-рейтинга для читаемости.
     */
    private void extractMpaRating(Film film, ResultSet rs) throws SQLException {
        long mpaId = rs.getLong("mpa_rating_id");

        // wasNull() проверяет, был ли NULL в базе (для LEFT JOIN)
        if (!rs.wasNull()) {
            String mpaName = rs.getString("mpa_name");
            film.setMpa(Mpa.of(mpaId, mpaName));
        }
    }
}
