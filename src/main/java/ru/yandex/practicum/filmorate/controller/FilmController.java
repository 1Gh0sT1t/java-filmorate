package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {

    private final Map<Integer, Film> films = new HashMap<>();
    private int nextId = 1;

    @PostMapping
    public Film create(@RequestBody Film film) {
        validate(film);

        film.setId(nextId++);
        films.put(film.getId(), film);

        log.info("Film created: {}", film);
        return film;
    }

    @PutMapping
    public Film update(@RequestBody Film film) {
        if (!films.containsKey(film.getId())) {
            throw new ValidationException("Film with id " + film.getId() + " not found");
        }

        validate(film);
        films.put(film.getId(), film);

        log.info("Film updated: {}", film);
        return film;
    }

    @GetMapping
    public Collection<Film> findAll() {
        return films.values();
    }

    private void validate(Film film) {
        if (film.getName() == null || film.getName().isBlank()) {
            throw new ValidationException("Film name is empty");
        }
        if (film.getDescription() != null && film.getDescription().length() > 200) {
            throw new ValidationException("Description is too long");
        }
        if (film.getReleaseDate() != null &&
                film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            throw new ValidationException("Release date is too early");
        }
        if (film.getDuration() <= 0) {
            throw new ValidationException("Duration must be positive");
        }
    }
}
