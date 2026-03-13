package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.MpaStorage;

import java.util.Collection;

@RestController
@RequestMapping("/mpa")
@RequiredArgsConstructor
public class MpaController {

    private final MpaStorage mpaStorage;

    // Получаем список всех рейтингов
    @GetMapping
    public Collection<Mpa> findAll() {
        return mpaStorage.findAll();
    }

    // Получаем рейтинг по id
    @GetMapping("/{id}")
    public Mpa findById(@PathVariable int id) {
        return mpaStorage.findById(id);
    }
}