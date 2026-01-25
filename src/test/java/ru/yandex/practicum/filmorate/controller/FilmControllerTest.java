package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertThrows;

class FilmControllerTest {

    private final FilmController controller = new FilmController();

    @Test
    void shouldThrowExceptionIfNameIsEmpty() {
        Film film = new Film();
        film.setName("");
        film.setDescription("description");
        film.setReleaseDate(LocalDate.now());
        film.setDuration(100);

        assertThrows(ValidationException.class, () -> controller.create(film));
    }

    @Test
    void shouldThrowExceptionIfDurationIsNegative() {
        Film film = new Film();
        film.setName("Film");
        film.setDescription("description");
        film.setReleaseDate(LocalDate.now());
        film.setDuration(-10);

        assertThrows(ValidationException.class, () -> controller.create(film));
    }

    @Test
    void shouldThrowExceptionIfReleaseDateTooEarly() {
        Film film = new Film();
        film.setName("Film");
        film.setDescription("description");
        film.setReleaseDate(LocalDate.of(1800, 1, 1));
        film.setDuration(100);

        assertThrows(ValidationException.class, () -> controller.create(film));
    }
}
