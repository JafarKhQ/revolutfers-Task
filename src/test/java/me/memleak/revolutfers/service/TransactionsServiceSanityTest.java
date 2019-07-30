package me.memleak.revolutfers.service;

import com.google.inject.AbstractModule;
import me.memleak.revolutfers.guicemodule.GuiceConfigurationModule;
import me.memleak.revolutfers.model.Account;
import me.memleak.revolutfers.model.Transaction;
import me.memleak.revolutfers.repository.AccountsInMemoryRepository;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Future;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

public class TransactionsServiceSanityTest extends BaseServiceTest {

  private TransactionsService transactionsService;
  private AccountsInMemoryRepository accountsRepository;

  @Override
  public void setUp() throws Exception {
    super.setUp();

    transactionsService = injector.getInstance(TransactionsService.class);
    accountsRepository = injector.getInstance(AccountsInMemoryRepository.class);
  }

  @Override
  public void tearDown() throws Exception {
    super.tearDown();

    transactionsService.stop();
    accountsRepository.deleteAll();
  }

  @Test
  public void transactionsProcessedInOrderTwoAccounts() throws Exception {
    // given
    prepareAccounts();
    List<Transaction> transactions = Arrays.asList(
        new Transaction(1, 2, BigDecimal.valueOf(5.0)),
        new Transaction(1, 2, BigDecimal.valueOf(1.0)),
        new Transaction(2, 1, BigDecimal.valueOf(3.0)),
        new Transaction(2, 1, BigDecimal.valueOf(2.0)),
        new Transaction(1, 2, BigDecimal.valueOf(2.0)),
        new Transaction(1, 2, BigDecimal.valueOf(2.0)),
        new Transaction(2, 1, BigDecimal.valueOf(5.0)),
        new Transaction(1, 2, BigDecimal.valueOf(2.0))
    );

    // when
    executeAndWait(transactions);

    // then
    assertThat(accountsRepository.find(1).get().getBalance()).isEqualTo(BigDecimal.valueOf(6.0));
    assertThat(accountsRepository.find(2).get().getBalance()).isEqualTo(BigDecimal.valueOf(2.0));
  }

  @Test
  public void transactionsProcessedInOrderMixedAccounts() throws Exception {
    // given
    prepareAccounts();
    List<Transaction> transactions = Arrays.asList(
        new Transaction(1, 2, BigDecimal.valueOf(5.0)),
        new Transaction(1, 2, BigDecimal.valueOf(1.0)),
        new Transaction(3, 4, BigDecimal.valueOf(3.0)),
        new Transaction(3, 4, BigDecimal.valueOf(2.0)),
        new Transaction(4, 3, BigDecimal.valueOf(1.0)),
        new Transaction(2, 1, BigDecimal.valueOf(2.0)),
        new Transaction(3, 4, BigDecimal.valueOf(1.0)),
        new Transaction(1, 2, BigDecimal.valueOf(2.0))
    );

    // when
    executeAndWait(transactions);

    // then
    assertThat(accountsRepository.find(1).get().getBalance()).isEqualTo(BigDecimal.valueOf(2.0));
    assertThat(accountsRepository.find(2).get().getBalance()).isEqualTo(BigDecimal.valueOf(6.0));
    assertThat(accountsRepository.find(3).get().getBalance()).isEqualTo(BigDecimal.valueOf(0.0));
    assertThat(accountsRepository.find(4).get().getBalance()).isEqualTo(BigDecimal.valueOf(5.0));
  }

  private void prepareAccounts() {
    accountsRepository.create(new Account(BigDecimal.valueOf(8.0)));
    accountsRepository.create(new Account(BigDecimal.valueOf(0.0)));
    accountsRepository.create(new Account(BigDecimal.valueOf(5.0)));
    accountsRepository.create(new Account(BigDecimal.valueOf(0.0)));
  }

  private void executeAndWait(List<Transaction> transactions) throws Exception {
    List<Future<Transaction>> futures = transactions.stream()
        .map(transactionsService::onNewTransaction)
        .collect(toList());
    // wait for all to finish
    for (Future<Transaction> future : futures) {
      future.get();
    }
  }

  @Override
  AbstractModule mockedGuiceModules() {
    return new GuiceConfigurationModule() {
      @Override
      protected void bindApp() {
        // server not needed
      }
    };
  }
}