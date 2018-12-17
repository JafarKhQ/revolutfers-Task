package me.memleak.revolutfers.service;

import me.memleak.revolutfers.controller.model.AccountRequest;
import me.memleak.revolutfers.exception.AccountNotFoundException;
import me.memleak.revolutfers.model.Account;
import me.memleak.revolutfers.repository.AccountMapRepository;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

import static me.memleak.revolutfers.util.BalanceUtil.toBankingBalance;

@Singleton
public class AccountService {

  private final AccountMapRepository repository;

  @Inject
  public AccountService(AccountMapRepository repository) {
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
    return repository.update(account);
  }

  public Account create(AccountRequest request) {
    return repository.create(
        new Account(toBankingBalance.apply(request.getBalance()))
    );
  }
}
