package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;

public interface FilmStorage {

    // Добавляем фильм
    Film create(Film film);

    // Обновляем фильм
    Film update(Film film);

    // Получаем фильм по id
    Film getById(int id);

    // Получаем список всех фильмов
    Collection<Film> findAll();

    // Удаляем фильм
    void delete(int id);

    // Добавляем лайк
    void addLike(int filmId, int userId);

    // Удаляем лайк
    void removeLike(int filmId, int userId);
}
