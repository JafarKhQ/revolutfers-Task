package me.memleak.revolutfers.service;

import me.memleak.revolutfers.exception.AccountNotFoundException;
import me.memleak.revolutfers.model.Account;
import me.memleak.revolutfers.repository.AccountMapRepository;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.function.DoubleFunction;

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

  public Account create(double balance) {
    return repository.create(toBankingBalance.apply(balance));
  }

  DoubleFunction<BigDecimal> toBankingBalance =
      (d) -> BigDecimal.valueOf(d).setScale(2, RoundingMode.CEILING);
}
