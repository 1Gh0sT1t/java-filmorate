package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.GenreStorage;
import ru.yandex.practicum.filmorate.storage.MpaStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FilmService {

    // Работаем с хранилищем фильмов
    private final FilmStorage filmStorage;

    // Нужно чтобы проверять существование пользователя
    private final UserStorage userStorage;

    // Хранилище рейтингов MPA
    private final MpaStorage mpaStorage;

    // Хранилище жанров
    private final GenreStorage genreStorage;

    // Нужен доступ к таблицам связей фильма
    private final FilmDbStorage filmDbStorage;

    // Минимальная допустимая дата релиза
    private static final LocalDate MIN_RELEASE_DATE = LocalDate.of(1895, 12, 28);

    // Создаем новый фильм
    public Film create(Film film) {
        validateFilm(film);
        validateMpa(film);
        validateGenres(film);

        Film createdFilm = filmStorage.create(film);
        loadRelationsForFilms(java.util.List.of(createdFilm));

        return createdFilm;
    }

    // Обновляем фильм
    public Film update(Film film) {
        validateFilm(film);
        validateMpa(film);
        validateGenres(film);

        Film updatedFilm = filmStorage.update(film);
        loadRelationsForFilms(java.util.List.of(updatedFilm));

        return updatedFilm;
    }

    // Получаем фильм по id
    public Film getById(int id) {
        Film film = filmStorage.getById(id);
        loadRelationsForFilms(java.util.List.of(film));
        return film;
    }

    // Получаем список всех фильмов
    public Collection<Film> findAll() {
        Collection<Film> films = filmStorage.findAll();
        loadRelationsForFilms(films);
        return films;
    }

    // Добавляем лайк фильму
    public void addLike(int filmId, int userId) {
        userStorage.getById(userId); // проверяем что пользователь существует
        filmStorage.addLike(filmId, userId);
    }

    // Удаляем лайк у фильма
    public void removeLike(int filmId, int userId) {
        userStorage.getById(userId); // проверяем что пользователь существует
        filmStorage.removeLike(filmId, userId);
    }

    // Получаем популярные фильмы
    public Collection<Film> getPopular(int count) {
        Collection<Film> films = filmStorage.getPopular(count);
        loadRelationsForFilms(films);
        return films;
    }

    // Проверяем корректность фильма
    private void validateFilm(Film film) {
        if (film.getReleaseDate().isBefore(MIN_RELEASE_DATE)) {
            throw new ValidationException("Дата релиза слишком ранняя");
        }
    }

    // Проверяем что указанный рейтинг MPA существует
    private void validateMpa(Film film) {
        if (film.getMpa() != null) {
            mpaStorage.findById(film.getMpa().getId());
        }
    }

    // Проверяем что указанные жанры существуют
    private void validateGenres(Film film) {
        if (film.getGenres() != null) {
            for (Genre genre : film.getGenres()) {
                genreStorage.findById(genre.getId());
            }
        }
    }

    // Загружаем жанры и лайки сразу для всех фильмов
    private void loadRelationsForFilms(Collection<Film> films) {
        if (films == null || films.isEmpty()) {
            return;
        }

        Set<Integer> filmIds = films.stream()
                .map(Film::getId)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        Map<Integer, Set<Integer>> genreIdsByFilmId = filmDbStorage.getGenreIdsByFilmIds(filmIds);
        Map<Integer, Set<Integer>> likesByFilmId = filmDbStorage.getLikesByFilmIds(filmIds);

        Set<Integer> allGenreIds = genreIdsByFilmId.values().stream()
                .flatMap(Set::stream)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        Map<Integer, Genre> genresById = genreStorage.findByIds(allGenreIds);

        for (Film film : films) {
            Set<Genre> genres = new LinkedHashSet<>();
            Set<Integer> genreIds = genreIdsByFilmId.getOrDefault(film.getId(), new LinkedHashSet<>());

            for (Integer genreId : genreIds) {
                Genre genre = genresById.get(genreId);
                if (genre != null) {
                    genres.add(genre);
                }
            }

            film.setGenres(genres);
            film.setLikes(likesByFilmId.getOrDefault(film.getId(), new LinkedHashSet<>()));
        }
    }
}