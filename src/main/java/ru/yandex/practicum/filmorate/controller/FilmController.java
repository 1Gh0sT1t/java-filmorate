package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.Collection;

@Slf4j
@RestController
@RequestMapping("/films")
@RequiredArgsConstructor
public class FilmController {

    // Сервис для работы с фильмами
    private final FilmService filmService;

    // Создаем новый фильм
    @PostMapping
    public Film create(@Valid @RequestBody Film film) {
        log.info("Создание фильма");
        return filmService.create(film);
    }

    // Обновляем фильм
    @PutMapping
    public Film update(@Valid @RequestBody Film film) {
        log.info("Обновление фильма");
        return filmService.update(film);
    }

    // Получаем список всех фильмов
    @GetMapping
    public Collection<Film> findAll() {
        return filmService.findAll();
    }

    // Получаем фильм по id
    @GetMapping("/{id}")
    public Film getById(@PathVariable int id) {
        return filmService.getById(id);
    }

    // Добавляем лайк фильму
    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable int id, @PathVariable int userId) {
        filmService.addLike(id, userId);
    }

    // Удаляем лайк у фильма
    @DeleteMapping("/{id}/like/{userId}")
    public void removeLike(@PathVariable int id, @PathVariable int userId) {
        filmService.removeLike(id, userId);
    }

    // Получаем список популярных фильмов
    @GetMapping("/popular")
    public Collection<Film> getPopular(@RequestParam(defaultValue = "10") int count) {
        return filmService.getPopular(count);
    }
}