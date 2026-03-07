package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import(MpaDbStorage.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class MpaDbStorageTest {

    private final MpaDbStorage mpaStorage;

    @Test
    void shouldReturnMpaList() {
        Collection<Mpa> mpaList = mpaStorage.findAll();
        assertThat(mpaList).isNotEmpty();
    }

    @Test
    void shouldFindMpaById() {
        Mpa mpa = mpaStorage.findById(1);
        assertThat(mpa).isNotNull();
        assertThat(mpa.getId()).isEqualTo(1);
    }
}