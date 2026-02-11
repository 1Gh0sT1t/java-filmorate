package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserStorage userStorage;

    // создаем пользователя
    public User create(User user) {
        return userStorage.create(user);
    }

    // обновляем пользователя
    public User update(User user) {
        return userStorage.update(user);
    }

    // получаем всех пользователей
    public Collection<User> findAll() {
        return userStorage.findAll();
    }

    // получаем пользователя по id
    public User getById(int id) {
        return userStorage.getById(id);
    }

    // добавить в друзья
    public void addFriend(int id, int friendId) {
        userStorage.addFriend(id, friendId);
    }

    // удалить из друзей
    public void removeFriend(int id, int friendId) {
        userStorage.removeFriend(id, friendId);
    }

    // список друзей
    public Set<User> getFriends(int id) {
        return userStorage.getFriends(id);
    }

    // общие друзья
    public Set<User> getCommonFriends(int id, int otherId) {
        return userStorage.getCommonFriends(id, otherId);
    }
}
