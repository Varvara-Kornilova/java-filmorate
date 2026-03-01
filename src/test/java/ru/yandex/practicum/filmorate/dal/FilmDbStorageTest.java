package ru.yandex.practicum.filmorate.dal;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import(FilmDbStorage.class)
class FilmDbStorageTest {

    private final FilmDbStorage filmStorage;

    @Test
    void shouldCreateFilm() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Test Description");
        film.setReleaseDate(LocalDate.of(2024, 1, 1));
        film.setDuration(120);
        film.setMpa(new Mpa(1L, "G", "General"));

        Film created = filmStorage.create(film);

        assertThat(created.getId()).isNotNull();
        assertThat(created.getId()).isPositive();
        assertThat(created.getName()).isEqualTo("Test Film");
        assertThat(created.getDuration()).isEqualTo(120);
        assertThat(created.getMpa()).isNotNull();
        assertThat(created.getMpa().getName()).isEqualTo("G");
    }

    @Test
    void shouldCreateFilmWithoutMpa() {
        Film film = new Film();
        film.setName("Film Without MPA");
        film.setDescription("No Rating");
        film.setReleaseDate(LocalDate.of(2024, 1, 1));
        film.setDuration(80);

        Film created = filmStorage.create(film);

        assertThat(created.getId()).isNotNull();
        assertThat(created.getMpa()).isNull();
    }

    @Test
    void shouldUpdateFilm() {
        Film film = new Film();
        film.setName("Old Film");
        film.setDescription("Old Description");
        film.setReleaseDate(LocalDate.of(2024, 1, 1));
        film.setDuration(100);
        Film created = filmStorage.create(film);

        created.setName("Updated Film");
        created.setDescription("Updated Description");
        created.setDuration(150);

        Film updated = filmStorage.update(created);

        assertThat(updated.getName()).isEqualTo("Updated Film");
        assertThat(updated.getDescription()).isEqualTo("Updated Description");
        assertThat(updated.getDuration()).isEqualTo(150);
        assertThat(updated.getId()).isEqualTo(created.getId());
    }

    @Test
    void shouldFindFilmById_whenExists() {
        Film film = new Film();
        film.setName("Find Me Film");
        film.setDescription("Find Me Description");
        film.setReleaseDate(LocalDate.of(2024, 1, 1));
        film.setDuration(90);
        Film created = filmStorage.create(film);

        Optional<Film> found = filmStorage.findById(created.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(created.getId());
        assertThat(found.get().getName()).isEqualTo("Find Me Film");
    }

    @Test
    void shouldReturnEmpty_whenFilmNotFound() {
        Optional<Film> found = filmStorage.findById(999L);

        assertThat(found).isEmpty();
    }

    @Test
    void shouldFindAllFilms() {
        Film film1 = new Film();
        film1.setName("Film One");
        film1.setDescription("Description One");
        film1.setReleaseDate(LocalDate.of(2024, 1, 1));
        film1.setDuration(100);

        Film film2 = new Film();
        film2.setName("Film Two");
        film2.setDescription("Description Two");
        film2.setReleaseDate(LocalDate.of(2024, 2, 2));
        film2.setDuration(120);

        filmStorage.create(film1);
        filmStorage.create(film2);

        List<Film> films = filmStorage.findAllFilms();

        assertThat(films).hasSize(2);
        assertThat(films).extracting("name").containsExactlyInAnyOrder("Film One", "Film Two");
    }

    @Test
    void shouldReturnEmptyList_whenNoFilms() {
        List<Film> films = filmStorage.findAllFilms();

        assertThat(films).isEmpty();
    }
}
