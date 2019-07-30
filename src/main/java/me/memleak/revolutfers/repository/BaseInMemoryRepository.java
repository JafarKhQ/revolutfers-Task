package me.memleak.revolutfers.repository;

import me.memleak.revolutfers.exception.ItemNotFoundException;
import me.memleak.revolutfers.model.ModelId;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public abstract class BaseInMemoryRepository<T extends ModelId> {
  private static final long ID_INIT_VALUE = 1L;

  private final AtomicLong idGenerator = new AtomicLong(ID_INIT_VALUE);
  private final Map<Long, T> mapDB = new ConcurrentHashMap<>();

  public T create(T item) {
    final long id = getNextId();
    item.setId(id);
    mapDB.put(id, item);

    return item;
  }

  public T update(T item) {
    Long id = item.getId();
    if (id == null || !mapDB.containsKey(id)) {
      throw new ItemNotFoundException("Item with id {0} not persisted in db", id);
    }

    mapDB.replace(id, item);
    return item;
  }

  public List<T> findAll() {
    return new ArrayList<>(mapDB.values());
  }

  public Optional<T> find(long id) {
    return Optional.ofNullable(mapDB.get(id));
  }

  public void deleteAll() {
    mapDB.clear();
    idGenerator.set(ID_INIT_VALUE);
  }

  private long getNextId() {
    return idGenerator.getAndIncrement();
  }
}
