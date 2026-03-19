package ru.yandex.practicum.filmorate.dal;

import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.Collection;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class MpaDbStorageTest extends BaseJdbcTest {

    @Test
    public void testFindAll() {
        Collection<Mpa> mpas = mpaStorage.findAll();

        assertThat(mpas).isNotEmpty();
        assertThat(mpas).extracting("id").containsExactlyInAnyOrder(1L, 2L, 3L, 4L, 5L);
        assertThat(mpas).extracting("name").containsExactlyInAnyOrder("G", "PG", "PG-13", "R", "NC-17");
    }

    @Test
    public void testFindById() {
        Optional<Mpa> found = mpaStorage.findById(1L);
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("G");
        assertThat(found.get().getDescription()).isNotBlank();
    }

    @Test
    public void testFindByIdNotFound() {
        Optional<Mpa> found = mpaStorage.findById(999L);
        assertThat(found).isEmpty();
    }
}
