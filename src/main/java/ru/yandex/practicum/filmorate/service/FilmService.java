package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FilmService {

    // Работаем через интерфейс хранилища фильмов
    private final FilmStorage filmStorage;

    // Нужно чтобы проверять существование пользователя
    private final UserStorage userStorage;

    // Добавляем фильм
    public Film create(Film film) {
        return filmStorage.create(film);
    }

    // Обновляем фильм
    public Film update(Film film) {
        return filmStorage.update(film);
    }

    // Получаем фильм по id
    public Film getById(int id) {
        return filmStorage.getById(id);
    }

    // Получаем все фильмы
    public Collection<Film> findAll() {
        return filmStorage.findAll();
    }

    // Ставим лайк
    public void addLike(int filmId, int userId) {
        Film film = filmStorage.getById(filmId);
        userStorage.getById(userId);

        film.getLikes().add(userId);
    }

    // Удаляем лайк
    public void removeLike(int filmId, int userId) {
        Film film = filmStorage.getById(filmId);
        userStorage.getById(userId);

        film.getLikes().remove(userId);
    }

    // Получаем популярные фильмы
    public List<Film> getPopular(int count) {
        return filmStorage.findAll().stream()
                .sorted(Comparator.comparingInt(f -> -f.getLikes().size()))
                .limit(count)
                .collect(Collectors.toList());
    }
}
