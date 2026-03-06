package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;
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

    // Минимальная допустимая дата релиза
    private static final LocalDate MIN_RELEASE_DATE = LocalDate.of(1895, 12, 28);

    // Добавляем фильм
    public Film create(Film film) {
        validateFilm(film);
        return filmStorage.create(film);
    }

    // Обновляем фильм
    public Film update(Film film) {
        validateFilm(film);
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
        userStorage.getById(userId); // проверяем существование пользователя
        filmStorage.addLike(filmId, userId);
    }

    // Удаляем лайк
    public void removeLike(int filmId, int userId) {
        userStorage.getById(userId); // проверяем существование пользователя
        filmStorage.removeLike(filmId, userId);
    }

    // Получаем популярные фильмы
    public List<Film> getPopular(int count) {
        return filmStorage.findAll().stream()
                .sorted(Comparator.comparingInt((Film f) -> f.getLikes().size()).reversed())
                .limit(count)
                .collect(Collectors.toList());
    }

    // Проверка фильма
    private void validateFilm(Film film) {
        if (film.getReleaseDate().isBefore(MIN_RELEASE_DATE)) {
            throw new ValidationException("Release date is too early");
        }
    }
}