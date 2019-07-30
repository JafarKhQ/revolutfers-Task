package me.memleak.revolutfers.service;

import com.google.inject.AbstractModule;
import me.memleak.revolutfers.controller.model.AccountRequest;
import me.memleak.revolutfers.exception.AccountNotFoundException;
import me.memleak.revolutfers.model.Account;
import me.memleak.revolutfers.repository.AccountsInMemoryRepository;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;
import static org.mockito.Mockito.*;

public class AccountsServiceTest extends BaseServiceTest {

  private static final Long ACCOUNT_ID = 0L;

  private AccountsService uut;
  private AccountsInMemoryRepository repository;

  @Override
  public void setUp() throws Exception {
    super.setUp();

    uut = injector.getInstance(AccountsService.class);
    repository = injector.getInstance(AccountsInMemoryRepository.class);
  }

  @Override
  public void tearDown() throws Exception {
    verifyNoMoreInteractions(repository);
  }

  @Test
  public void create() {
    Account expected = new Account(ACCOUNT_ID, BigDecimal.ONE);
    when(repository.create(any(Account.class))).thenReturn(expected);

    AccountRequest request = new AccountRequest();
    request.setBalance(1.00);
    Account result = uut.create(request);

    ArgumentCaptor<Account> accountCaptor = ArgumentCaptor.forClass(Account.class);
    verify(repository, only()).create(accountCaptor.capture());

    assertThat(result).isEqualTo(expected);
    assertThat(accountCaptor.getValue().getId()).isNull();
    assertThat(accountCaptor.getValue().getBalance()).isEqualByComparingTo("1");
  }

  @Test
  public void getAll() {
    List<Account> expected = new ArrayList<>();
    when(repository.findAll()).thenReturn(expected);

    List<Account> result = uut.getAll();

    assertThat(result).isEqualTo(expected);
    verify(repository, only()).findAll();
  }

  @Test
  public void find() {
    Account expected = new Account(ACCOUNT_ID);
    when(repository.find(eq(ACCOUNT_ID))).thenReturn(Optional.of(expected));

    Account result = uut.get(ACCOUNT_ID);

    assertThat(result).isEqualTo(expected);
    verify(repository, only()).find(eq(ACCOUNT_ID));
  }

  @Test
  public void find_accountNotFound() {
    when(repository.find(eq(ACCOUNT_ID))).thenReturn(Optional.empty());

    try {
      uut.get(ACCOUNT_ID);
      failBecauseExceptionWasNotThrown(AccountNotFoundException.class);
    } catch (AccountNotFoundException e) {
      verify(repository, only()).find(eq(ACCOUNT_ID));
    }
  }

  @Test
  public void update() {
    //given
    Lock lock = new ReentrantLock();
    Account account = new Account(ACCOUNT_ID, BigDecimal.ONE);
    when(repository.find(eq(ACCOUNT_ID))).thenReturn(Optional.of(account));
    when(repository.findLockById(eq(ACCOUNT_ID))).thenReturn(Optional.of(lock));

    //when
    uut.update(account);

    //then
    verify(repository).find(eq(ACCOUNT_ID));
    verify(repository).update(eq(account));
    verify(repository, times(2)).findLockById(eq(ACCOUNT_ID));
  }

  @Test
  public void lockAccountsMustLockInOrder() {
    //given
    Account src = new Account(5L), dest = new Account(3L);
    InOrder inOrder = inOrder(repository);
    when(repository.findLockById(anyLong())).thenReturn(Optional.of(new ReentrantLock()));

    //when
    uut.lockAccounts(src.getId(), dest.getId());

    //then
    inOrder.verify(repository).findLockById(eq(3L));
    inOrder.verify(repository).findLockById(eq(5L));
  }

  @Test
  public void unlockAccountsMustUnLockInOrder() {
    //given
    Account src = new Account(7L), dest = new Account(1L);
    InOrder inOrder = inOrder(repository);
    when(repository.findLockById(anyLong())).thenAnswer(a -> {
      Lock lock = new ReentrantLock();
      lock.lock();
      return Optional.of(lock);
    });

    //when
    uut.unlockAccounts(src.getId(), dest.getId());

    //then
    inOrder.verify(repository).findLockById(eq(1L));
    inOrder.verify(repository).findLockById(eq(7L));
  }

  @Override
  AbstractModule mockedGuiceModules() {
    return new AbstractModule() {
      @Override
      protected void configure() {
        bind(AccountsInMemoryRepository.class)
            .toInstance(mock(AccountsInMemoryRepository.class));
      }
    };
  }
}
