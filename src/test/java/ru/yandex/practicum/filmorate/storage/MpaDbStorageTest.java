package ru.yandex.practicum.filmorate.storage;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class MpaDbStorageTest {

    @Autowired
    private MpaDbStorage mpaStorage;

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