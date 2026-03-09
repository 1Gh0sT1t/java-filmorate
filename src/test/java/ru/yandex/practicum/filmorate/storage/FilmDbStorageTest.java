package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import({FilmDbStorage.class, UserDbStorage.class})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class FilmDbStorageTest {

    private final FilmDbStorage filmStorage;
    private final UserDbStorage userStorage;

    // Проверяем создание фильма
    @Test
    void shouldCreateFilm() {
        Film film = new Film();
        film.setName("Film");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);

        Mpa mpa = new Mpa();
        mpa.setId(1);
        film.setMpa(mpa);

        Film created = filmStorage.create(film);

        assertThat(created.getId()).isPositive();

        Film fromDb = filmStorage.getById(created.getId());

        assertThat(fromDb.getName()).isEqualTo("Film");
    }

    // Проверяем обновление фильма
    @Test
    void shouldUpdateFilm() {
        Film film = new Film();
        film.setName("Film");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);

        Mpa mpa = new Mpa();
        mpa.setId(1);
        film.setMpa(mpa);

        Film created = filmStorage.create(film);

        created.setName("Updated");

        filmStorage.update(created);

        Film updated = filmStorage.getById(created.getId());

        assertThat(updated.getName()).isEqualTo("Updated");
    }

    // Проверяем сохранение жанров фильма
    @Test
    void shouldSaveGenres() {
        Film film = new Film();
        film.setName("Film");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);

        Mpa mpa = new Mpa();
        mpa.setId(1);
        film.setMpa(mpa);

        Genre g1 = new Genre();
        g1.setId(1);

        Genre g2 = new Genre();
        g2.setId(2);

        film.setGenres(Set.of(g1, g2));

        Film created = filmStorage.create(film);

        Map<Integer, Set<Integer>> genreIdsByFilmId =
                filmStorage.getGenreIdsByFilmIds(Set.of(created.getId()));

        assertThat(genreIdsByFilmId.get(created.getId())).hasSize(2);
        assertThat(genreIdsByFilmId.get(created.getId())).containsExactly(1, 2);
    }

    // Проверяем добавление лайка
    @Test
    void shouldAddLike() {
        Film film = new Film();
        film.setName("Film");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);

        Mpa mpa = new Mpa();
        mpa.setId(1);
        film.setMpa(mpa);

        Film createdFilm = filmStorage.create(film);

        User user = new User();
        user.setEmail("test@test.com");
        user.setLogin("login");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        User createdUser = userStorage.create(user);

        filmStorage.addLike(createdFilm.getId(), createdUser.getId());

        Map<Integer, Set<Integer>> likesByFilmId =
                filmStorage.getLikesByFilmIds(Set.of(createdFilm.getId()));

        assertThat(likesByFilmId.get(createdFilm.getId())).hasSize(1);
        assertThat(likesByFilmId.get(createdFilm.getId())).contains(createdUser.getId());
    }

    // Проверяем удаление лайка
    @Test
    void shouldRemoveLike() {
        Film film = new Film();
        film.setName("Film");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);

        Mpa mpa = new Mpa();
        mpa.setId(1);
        film.setMpa(mpa);

        Film createdFilm = filmStorage.create(film);

        User user = new User();
        user.setEmail("test@test.com");
        user.setLogin("login");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        User createdUser = userStorage.create(user);

        filmStorage.addLike(createdFilm.getId(), createdUser.getId());
        filmStorage.removeLike(createdFilm.getId(), createdUser.getId());

        Map<Integer, Set<Integer>> likesByFilmId =
                filmStorage.getLikesByFilmIds(Set.of(createdFilm.getId()));

        assertThat(likesByFilmId.get(createdFilm.getId())).isEmpty();
    }

    // Проверяем получение популярных фильмов
    @Test
    void shouldReturnPopularFilms() {
        Film f1 = new Film();
        f1.setName("F1");
        f1.setDescription("d1");
        f1.setReleaseDate(LocalDate.of(2000, 1, 1));
        f1.setDuration(100);

        Mpa mpa = new Mpa();
        mpa.setId(1);
        f1.setMpa(mpa);

        Film f2 = new Film();
        f2.setName("F2");
        f2.setDescription("d2");
        f2.setReleaseDate(LocalDate.of(2000, 1, 1));
        f2.setDuration(100);
        f2.setMpa(mpa);

        Film film1 = filmStorage.create(f1);
        Film film2 = filmStorage.create(f2);

        User user = new User();
        user.setEmail("user@test.com");
        user.setLogin("user");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        User createdUser = userStorage.create(user);

        filmStorage.addLike(film1.getId(), createdUser.getId());

        Collection<Film> popular = filmStorage.getPopular(1);

        assertThat(popular).hasSize(1);
        assertThat(popular.iterator().next().getId()).isEqualTo(film1.getId());
    }
}