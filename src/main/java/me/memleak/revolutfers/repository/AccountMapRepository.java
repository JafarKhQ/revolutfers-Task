package me.memleak.revolutfers.repository;

import me.memleak.revolutfers.model.Account;

import javax.inject.Singleton;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Singleton
public class AccountMapRepository extends BaseMapRepository<Account> {

  private final Map<Long, Lock> mapDBLock = new ConcurrentHashMap<>();

  @Override
  public Account create(Account item) {
    Account account = super.create(item);
    mapDBLock.put(account.getId(), new ReentrantLock());
    return account;
  }

  public Optional<Lock> findLockById(long id) {
    return Optional.ofNullable(mapDBLock.get(id));
  }
}
