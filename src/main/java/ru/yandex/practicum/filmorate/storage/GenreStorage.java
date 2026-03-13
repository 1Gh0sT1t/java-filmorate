package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public interface GenreStorage {

    // Получить список всех жанров
    Collection<Genre> findAll();

    // Получить жанр по id
    Genre findById(int id);

    // Получить жанры сразу по набору id
    Map<Integer, Genre> findByIds(Set<Integer> ids);
}