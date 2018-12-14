package me.memleak.revolutfers.repository;

import me.memleak.revolutfers.model.Account;

import javax.inject.Singleton;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Singleton
public class AccountMapRepository {

  private final AtomicLong idGenerator = new AtomicLong();
  private final Map<Long, Account> accounts = new HashMap<>();

  public Account create(BigDecimal balance) {
    long id = idGenerator.getAndIncrement();
    Account account = new Account(id, balance);
    accounts.put(id, account);
    return account;
  }

  public List<Account> findAll() {
    return new ArrayList<>(accounts.values());
  }

  public Optional<Account> find(long id) {
    return Optional.ofNullable(accounts.get(id));
  }
}
