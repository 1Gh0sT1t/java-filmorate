package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Repository
@Primary
@RequiredArgsConstructor
public class UserDbStorage implements UserStorage {

    private final JdbcTemplate jdbcTemplate;

    // создаем нового пользователя в базе
    @Override
    public User create(User user) {
        String sql = "INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?)";

        jdbcTemplate.update(sql,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday());

        Integer id = jdbcTemplate.queryForObject("SELECT MAX(id) FROM users", Integer.class);
        user.setId(id);

        return user;
    }

    // обновляем данные пользователя
    @Override
    public User update(User user) {
        String sql = "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE id = ?";

        int rowsUpdated = jdbcTemplate.update(sql,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday(),
                user.getId());

        if (rowsUpdated == 0) {
            throw new NotFoundException("Пользователь с id " + user.getId() + " не найден");
        }

        return user;
    }

    // получаем пользователя по id
    @Override
    public User getById(int id) {
        String sql = "SELECT * FROM users WHERE id = ?";

        List<User> users = jdbcTemplate.query(sql, (rs, rowNum) -> {
            User user = new User();

            user.setId(rs.getInt("id"));
            user.setEmail(rs.getString("email"));
            user.setLogin(rs.getString("login"));
            user.setName(rs.getString("name"));
            user.setBirthday(rs.getDate("birthday").toLocalDate());

            return user;
        }, id);

        if (users.isEmpty()) {
            throw new NotFoundException("Пользователь с id " + id + " не найден");
        }

        return users.get(0);
    }

    // получаем список всех пользователей
    @Override
    public Collection<User> findAll() {
        String sql = "SELECT * FROM users";

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            User user = new User();

            user.setId(rs.getInt("id"));
            user.setEmail(rs.getString("email"));
            user.setLogin(rs.getString("login"));
            user.setName(rs.getString("name"));
            user.setBirthday(rs.getDate("birthday").toLocalDate());

            return user;
        });
    }

    // удаляем пользователя
    @Override
    public void delete(int id) {
        getById(id);

        String sql = "DELETE FROM users WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    // добавляем пользователя в друзья
    @Override
    public void addFriend(int userId, int friendId) {

        // проверяем существование пользователей
        getById(userId);
        getById(friendId);

        String sql = "INSERT INTO friendships (user_id, friend_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, userId, friendId);
    }

    // удаляем пользователя из друзей
    @Override
    public void removeFriend(int userId, int friendId) {

        // проверяем существование пользователей
        getById(userId);
        getById(friendId);

        String sql = "DELETE FROM friendships WHERE user_id = ? AND friend_id = ?";
        jdbcTemplate.update(sql, userId, friendId);
    }

    // получаем список друзей пользователя
    @Override
    public Set<User> getFriends(int userId) {

        // проверяем существование пользователя
        getById(userId);

        String sql = """
                SELECT u.*
                FROM users u
                JOIN friendships f ON u.id = f.friend_id
                WHERE f.user_id = ?
                """;

        return new HashSet<>(jdbcTemplate.query(sql, (rs, rowNum) -> {
            User user = new User();

            user.setId(rs.getInt("id"));
            user.setEmail(rs.getString("email"));
            user.setLogin(rs.getString("login"));
            user.setName(rs.getString("name"));
            user.setBirthday(rs.getDate("birthday").toLocalDate());

            return user;
        }, userId));
    }

    // получаем список общих друзей
    @Override
    public Set<User> getCommonFriends(int userId, int otherId) {

        // проверяем существование пользователей
        getById(userId);
        getById(otherId);

        String sql = """
                SELECT u.*
                FROM users u
                JOIN friendships f1 ON u.id = f1.friend_id
                JOIN friendships f2 ON u.id = f2.friend_id
                WHERE f1.user_id = ? AND f2.user_id = ?
                """;

        return new HashSet<>(jdbcTemplate.query(sql, (rs, rowNum) -> {
            User user = new User();

            user.setId(rs.getInt("id"));
            user.setEmail(rs.getString("email"));
            user.setLogin(rs.getString("login"));
            user.setName(rs.getString("name"));
            user.setBirthday(rs.getDate("birthday").toLocalDate());

            return user;
        }, userId, otherId));
    }
}