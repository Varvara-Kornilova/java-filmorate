package ru.yandex.practicum.filmorate.dal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class DirectorDbStorageTest extends BaseJdbcTest {

    @Test
    public void testFindAll() {
        Collection<Director> directors = directorStorage.findAll();

        assertThat(directors).isNotEmpty();
        assertThat(directors).extracting("id").containsExactlyInAnyOrder(1L, 2L, 3L, 4L, 5L);
        assertThat(directors).extracting("name").containsExactlyInAnyOrder(
                "Квентин Тарантино", "Кристофер Нолан", "Найт Шьямалан",
                "Стивен Спилберг", "Дэвид Финчер");
    }

    @Test
    public void testFindById() {
        Optional<Director> found = directorStorage.findById(1L);

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Квентин Тарантино");
    }

    @Test
    public void testFindByIdNotFound() {
        Optional<Director> found = directorStorage.findById(999L);
        assertThat(found).isEmpty();
    }

    @Test
    public void testCreateDirector() {
        Director newDirector = new Director();
        newDirector.setName("Denis Villeneuve");

        Director created = directorStorage.create(newDirector);

        assertThat(created.getId()).isNotNull();
        assertThat(created.getName()).isEqualTo("Denis Villeneuve");

        Optional<Director> found = directorStorage.findById(created.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Denis Villeneuve");
    }

    @Test
    public void testUpdateDirector() {
        Director updated = new Director(1L, "Quentin Tarantino Updated");
        Director result = directorStorage.update(updated);

        assertThat(result.getName()).isEqualTo("Quentin Tarantino Updated");

        Optional<Director> found = directorStorage.findById(1L);
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Quentin Tarantino Updated");
    }

    @Test
    public void testDeleteDirector() {
        directorStorage.delete(1L);
        Optional<Director> found = directorStorage.findById(1L);
        assertThat(found).isEmpty();
    }

    @Test
    public void testExists() {
        assertThat(directorStorage.exists(1L)).isTrue();
        assertThat(directorStorage.exists(999L)).isFalse();
    }

    @Test
    public void testSetAndGetDirectorsForFilm() {
        Film film = createTestFilm();
        Film created = filmStorage.create(film);

        Set<Long> directorIds = Set.of(1L, 2L);
        directorStorage.setDirectors(created.getId(), directorIds);

        Set<Director> found = directorStorage.getDirectorsByFilmId(created.getId());
        assertThat(found).extracting("id").containsExactlyInAnyOrder(1L, 2L);
    }

    @Test
    public void testUpdateFilmDirectors() {
        Film film = createTestFilm();
        Film created = filmStorage.create(film);

        directorStorage.setDirectors(created.getId(), Set.of(1L, 2L));
        directorStorage.updateFilmDirectors(created.getId());
        directorStorage.setDirectors(created.getId(), Set.of(3L));

        Set<Director> found = directorStorage.getDirectorsByFilmId(created.getId());
        assertThat(found).extracting("id").containsExactlyInAnyOrder(3L);
        assertThat(found).extracting("id").doesNotContain(1L, 2L);
    }

    @Test
    public void testSetDirectorsEmpty() {
        Film film = createTestFilm();
        Film created = filmStorage.create(film);
        directorStorage.setDirectors(created.getId(), Set.of());

        Set<Director> found = directorStorage.getDirectorsByFilmId(created.getId());
        assertThat(found).isEmpty();
    }

    @Test
    public void testSetDirectorsNull() {
        Film film = createTestFilm();
        Film created = filmStorage.create(film);
        directorStorage.setDirectors(created.getId(), null);

        Set<Director> found = directorStorage.getDirectorsByFilmId(created.getId());
        assertThat(found).isEmpty();
    }

    @Test
    public void testCascadeDeleteOnFilmDelete() {
        Film film = createTestFilm();
        Film created = filmStorage.create(film);

        directorStorage.setDirectors(created.getId(), Set.of(1L, 2L));
        filmStorage.delete(created.getId());

        Set<Director> found = directorStorage.getDirectorsByFilmId(created.getId());
        assertThat(found).isEmpty();
    }

    private Film createTestFilm() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Test Description");
        film.setReleaseDate(LocalDate.of(2024, 1, 1));
        film.setDuration(120);
        film.setMpa(new Mpa(1L, "G", null));
        return film;
    }
}
