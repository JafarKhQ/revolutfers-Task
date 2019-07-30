package me.memleak.revolutfers.service;

import me.memleak.revolutfers.controller.model.AccountRequest;
import me.memleak.revolutfers.exception.AccountNotFoundException;
import me.memleak.revolutfers.model.Account;
import me.memleak.revolutfers.repository.AccountsInMemoryRepository;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.function.Consumer;

import static me.memleak.revolutfers.util.BalanceUtil.toBankingBalance;

@Singleton
public class AccountsService {

  private final AccountsInMemoryRepository repository;

  @Inject
  public AccountsService(AccountsInMemoryRepository repository) {
    this.repository = repository;
  }

  public List<Account> getAll() {
    return repository.findAll();
  }

  public Account get(long id) {
    return repository.find(id)
        .orElseThrow(() -> new AccountNotFoundException("Cant find Account with id: {0}", id));
  }

  public Account update(Account account) {
    // check if its exist first
    get(account.getId());
    // lock (same Thread will not lock) then update
    lockAccounts(account.getId());
    Account updated = repository.update(account);
    unlockAccounts(account.getId());
    return updated;
  }

  public Account create(AccountRequest request) {
    return repository.create(
        new Account(toBankingBalance(request.getBalance()))
    );
  }

  public void lockAccounts(long... ids) {
    lockUnlockAccounts(Lock::lock, ids);
  }

  public void unlockAccounts(long... ids) {
    lockUnlockAccounts(Lock::unlock, ids);
  }

  private void lockUnlockAccounts(Consumer<? super Lock> action, long... ids) {
    Arrays.stream(ids)
        .sorted()
        .mapToObj(repository::findLockById)
        .map(Optional::get)
        .forEachOrdered(action);
  }
}
