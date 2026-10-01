package com.carrental.repository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/** Generic in-memory repository. Swap for a JDBC/JPA version later without touching the service. */
public class InMemoryRepository<T> {
    private final Map<String, T> store = new LinkedHashMap<>();
    private final Function<T, String> idExtractor;

    public InMemoryRepository(Function<T, String> idExtractor) {
        this.idExtractor = idExtractor;
    }

    public void save(T entity) {
        store.put(idExtractor.apply(entity), entity);
    }

    public Optional<T> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }

    public List<T> findAll() {
        return new ArrayList<>(store.values());
    }
}
