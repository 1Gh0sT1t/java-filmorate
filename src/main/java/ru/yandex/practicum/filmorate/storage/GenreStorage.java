package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Collection;

public interface GenreStorage {

    // Получить список всех жанров
    Collection<Genre> findAll();

    // Получить жанр по id
    Genre findById(int id);
}