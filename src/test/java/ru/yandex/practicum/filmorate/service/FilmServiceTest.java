package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class FilmServiceTest {

    private FilmService filmService;
    private UserService userService;

    @BeforeEach
    void setUp() {
        InMemoryFilmStorage filmStorage = new InMemoryFilmStorage();
        InMemoryUserStorage userStorage = new InMemoryUserStorage();

        filmService = new FilmService(filmStorage, userStorage);
        userService = new UserService(userStorage);
    }

    @Test
    void shouldAddLike() {
        Film film = new Film();
        film.setName("Film");
        film.setDescription("desc");
        film.setReleaseDate(LocalDate.now());
        film.setDuration(100);

        User user = new User();
        user.setEmail("test@test.ru");
        user.setLogin("login");
        user.setBirthday(LocalDate.now().minusYears(20));

        filmService.create(film);
        userService.create(user);

        filmService.addLike(film.getId(), user.getId());

        assertEquals(1, film.getLikes().size());
    }

    @Test
    void shouldReturnPopularFilms() {
        Film f1 = new Film();
        f1.setName("F1");
        f1.setDescription("d1");
        f1.setReleaseDate(LocalDate.now());
        f1.setDuration(100);

        Film f2 = new Film();
        f2.setName("F2");
        f2.setDescription("d2");
        f2.setReleaseDate(LocalDate.now());
        f2.setDuration(100);

        filmService.create(f1);
        filmService.create(f2);

        User user = new User();
        user.setEmail("u@u.ru");
        user.setLogin("u");
        user.setBirthday(LocalDate.now().minusYears(20));

        userService.create(user);

        filmService.addLike(f1.getId(), user.getId());

        assertEquals(f1.getId(), filmService.getPopular(1).get(0).getId());
    }

    @Test
    void shouldRemoveLike() {
        Film film = new Film();
        film.setName("Film");
        film.setDescription("desc");
        film.setReleaseDate(LocalDate.now());
        film.setDuration(100);

        User user = new User();
        user.setEmail("test@test.ru");
        user.setLogin("login");
        user.setBirthday(LocalDate.now().minusYears(20));

        filmService.create(film);
        userService.create(user);

        filmService.addLike(film.getId(), user.getId());
        filmService.removeLike(film.getId(), user.getId());

        assertEquals(0, film.getLikes().size());
    }

    @Test
    void shouldThrowIfFilmNotFound() {
        assertThrows(RuntimeException.class,
                () -> filmService.getById(999));
    }

}
