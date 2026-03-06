package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class GenreDbStorageTest {

    private final GenreDbStorage genreStorage;

    @Test
    void shouldReturnAllGenres() {

        Collection<Genre> genres = genreStorage.findAll();

        assertThat(genres).isNotEmpty();
        assertThat(genres.size()).isEqualTo(6);
    }

    @Test
    void shouldReturnGenreById() {

        Genre genre = genreStorage.findById(1);

        assertThat(genre).isNotNull();
        assertThat(genre.getId()).isEqualTo(1);
        assertThat(genre.getName()).isNotBlank();
    }

}