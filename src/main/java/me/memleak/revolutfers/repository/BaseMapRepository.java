package me.memleak.revolutfers.repository;

import me.memleak.revolutfers.model.ModelId;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public abstract class BaseMapRepository<T extends ModelId> {

  private final AtomicLong idGenerator = new AtomicLong();
  private final Map<Long, T> mapDB = new ConcurrentHashMap<>();

  public T create(T item) {
    final long id = getNextId();
    item.setId(id);
    mapDB.put(id, item);

    return item;
  }

  public T update(T item) {
    Long id = item.getId();
    if (id == null) {
      return create(item);
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

  private long getNextId() {
    return idGenerator.getAndIncrement();
  }
}
