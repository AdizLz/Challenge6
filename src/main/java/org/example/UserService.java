package org.example;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class UserService {
    private final Map<String, User> store = new ConcurrentHashMap<>();

    public UserService() {
        // Seed data opcional eliminado
    }

    public Collection<User> getAll() { return store.values(); }

    public User get(String id) { return store.get(id); }

    public void add(User user) { store.put(user.getId(), user); }

    public void update(String id, User user) { store.put(id, user); }

    public void delete(String id) { store.remove(id); }

    public boolean exists(String id) { return store.containsKey(id); }
}
