package me.memleak.revolutfers.repository;

import com.google.inject.AbstractModule;
import me.memleak.revolutfers.model.Account;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.Lock;

import static org.assertj.core.api.Assertions.assertThat;

public class AccountsInMemoryRepositoryTest extends BaseRepositoryTest {

  private AccountsInMemoryRepository uut;

  @Override
  public void setUp() throws Exception {
    super.setUp();

    uut = injector.getInstance(AccountsInMemoryRepository.class);
  }

  @Override
  public void tearDown() throws Exception {
    uut.deleteAll();
  }

  @Test
  public void create() {
    //given
    Account account = new Account(BigDecimal.TEN);

    //when
    Account result = uut.create(account);

    //then
    assertThat(result.getId()).isPositive();
    assertThat(result.getBalance()).isEqualTo(BigDecimal.TEN);
  }

  @Test
  public void find() {
    //given
    Account account = new Account(BigDecimal.TEN);
    Account created = uut.create(account);

    //when
    Optional<Account> result = uut.find(created.getId());
    Optional<Lock> lock = uut.findLockById(created.getId());

    //then
    assertThat(result).contains(created);
    assertThat(lock).isNotEmpty();
  }

  @Test
  public void findNonExistingAccount() {
    //when
    Optional<Account> result = uut.find(1);
    Optional<Lock> lock = uut.findLockById(1);

    //then
    assertThat(result).isEmpty();
    assertThat(lock).isEmpty();
  }

  @Test
  public void findAll() {
    //given
    Account created1 = uut.create(new Account(BigDecimal.TEN));
    Account created2 = uut.create(new Account(BigDecimal.ONE));

    //when
    List<Account> result = uut.findAll();

    //then
    assertThat(result).containsExactlyInAnyOrder(created1, created2);
  }

  @Test
  public void update() {
    //given
    Account created = uut.create(new Account(BigDecimal.TEN));
    created.setBalance(BigDecimal.ONE);

    //when
    Account result = uut.update(created);

    //then
    assertThat(result).isEqualTo(created);
  }

  @Test
  public void updateNonExistingWillCreateNew() {
    //given
    Account account = new Account(BigDecimal.TEN);

    //when
    Account result = uut.update(account);

    //then
    assertThat(result.getId()).isPositive();
    assertThat(result.getBalance()).isEqualTo(BigDecimal.TEN);
  }

  @Test
  public void deleteAll() {
    //given
    uut.create(new Account(BigDecimal.TEN));
    uut.create(new Account(BigDecimal.ONE));

    //when
    uut.deleteAll();

    //then
    List<Account> accounts = uut.findAll();
    assertThat(accounts).isEmpty();
  }

  @Override
  AbstractModule mockedGuiceModules() {
    return new AbstractModule() {
      @Override
      protected void configure() {
      }
    };
  }
}