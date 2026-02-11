package ru.yandex.practicum.filmorate.storage;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;

@Component
public class InMemoryUserStorage implements UserStorage {

    // здесь храним пользователей
    private final Map<Integer, User> users = new HashMap<>();

    // здесь храним друзей (id пользователя -> список id друзей)
    private final Map<Integer, Set<Integer>> friends = new HashMap<>();

    // счетчик для id
    private int nextId = 1;

    @Override
    public User create(User user) {
        // присваиваем id
        user.setId(nextId++);
        users.put(user.getId(), user);

        // создаем пустой список друзей
        friends.put(user.getId(), new HashSet<>());

        return user;
    }

    @Override
    public User update(User user) {
        // если пользователя нет — ошибка
        if (!users.containsKey(user.getId())) {
            throw new NotFoundException("User not found");
        }

        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User getById(int id) {
        User user = users.get(id);

        if (user == null) {
            throw new NotFoundException("User not found");
        }

        return user;
    }

    @Override
    public Collection<User> findAll() {
        return users.values();
    }

    @Override
    public void delete(int id) {
        users.remove(id);
        friends.remove(id);
    }

    @Override
    public void addFriend(int id, int friendId) {
        // проверяем, что оба пользователя существуют
        if (!users.containsKey(id) || !users.containsKey(friendId)) {
            throw new NotFoundException("User not found");
        }

        friends.get(id).add(friendId);
        friends.get(friendId).add(id);
    }

    @Override
    public void removeFriend(int id, int friendId) {
        if (!users.containsKey(id) || !users.containsKey(friendId)) {
            throw new NotFoundException("User not found");
        }

        friends.get(id).remove(friendId);
        friends.get(friendId).remove(id);
    }

    @Override
    public Set<User> getFriends(int id) {
        if (!users.containsKey(id)) {
            throw new NotFoundException("User not found");
        }

        Set<User> result = new HashSet<>();

        // пробегаемся по id друзей
        for (Integer friendId : friends.get(id)) {
            result.add(users.get(friendId));
        }

        return result;
    }

    @Override
    public Set<User> getCommonFriends(int id, int otherId) {
        if (!users.containsKey(id) || !users.containsKey(otherId)) {
            throw new NotFoundException("User not found");
        }

        Set<User> result = new HashSet<>();

        // берем друзей первого пользователя
        for (Integer friendId : friends.get(id)) {
            // если этот id есть у второго — добавляем
            if (friends.get(otherId).contains(friendId)) {
                result.add(users.get(friendId));
            }
        }

        return result;
    }
}
