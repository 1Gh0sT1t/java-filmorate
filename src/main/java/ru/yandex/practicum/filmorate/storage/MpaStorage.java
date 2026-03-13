package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.Collection;

public interface MpaStorage {

    // Получить список всех рейтингов MPA
    Collection<Mpa> findAll();

    // Получить рейтинг MPA по id
    Mpa findById(int id);
}