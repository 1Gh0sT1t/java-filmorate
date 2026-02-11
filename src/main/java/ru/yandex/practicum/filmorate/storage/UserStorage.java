package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.Set;

public interface UserStorage {

    // добавить пользователя
    User create(User user);

    // обновить пользователя
    User update(User user);

    // получить пользователя по id
    User getById(int id);

    // получить всех пользователей
    Collection<User> findAll();

    // удалить пользователя
    void delete(int id);

    // добавить в друзья
    void addFriend(int id, int friendId);

    // удалить из друзей
    void removeFriend(int id, int friendId);

    // получить список друзей
    Set<User> getFriends(int id);

    // получить общих друзей
    Set<User> getCommonFriends(int id, int otherId);
}
