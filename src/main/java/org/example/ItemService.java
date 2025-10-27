package org.example;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ItemService {
    private final Map<String, Item> store = new ConcurrentHashMap<>();
    private static final Gson gson = new Gson();

    public ItemService() {
        try (InputStream is = getClass().getResourceAsStream("/items.json")) {
            if (is != null) {
                Type listType = new TypeToken<List<Item>>() {}.getType();
                List<Item> items = gson.fromJson(new InputStreamReader(is, StandardCharsets.UTF_8), listType);
                if (items != null) {
                    for (Item it : items) {
                        if (it != null && it.getId() != null) {
                            store.put(it.getId(), it);
                        }
                    }
                }
            } else {
                System.err.println("items.json not found in resources");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Collection<Item> getAll() { return store.values(); }

    public Item get(String id) { return store.get(id); }

    public boolean exists(String id) { return store.containsKey(id); }
}
