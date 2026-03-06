package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.GenreStorage;

import java.util.Collection;

@RestController
@RequestMapping("/genres")
@RequiredArgsConstructor
public class GenreController {

    private final GenreStorage genreStorage;

    // Получаем список всех жанров
    @GetMapping
    public Collection<Genre> findAll() {
        return genreStorage.findAll();
    }

    // Получаем жанр по id
    @GetMapping("/{id}")
    public Genre findById(@PathVariable int id) {
        return genreStorage.findById(id);
    }
}