package ru.yandex.practicum.filmorate.dal;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import ru.yandex.practicum.filmorate.dal.mappers.MpaRowMapper;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.Collection;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({MpaDbStorage.class, MpaRowMapper.class})
@Sql(scripts = "/data.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
public class MpaDbStorageTest {

    private final MpaDbStorage mpaStorage;

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
