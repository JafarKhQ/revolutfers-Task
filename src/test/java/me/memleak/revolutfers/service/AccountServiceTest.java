package me.memleak.revolutfers.service;

import com.google.inject.AbstractModule;
import me.memleak.revolutfers.controller.model.AccountRequest;
import me.memleak.revolutfers.exception.AccountNotFoundException;
import me.memleak.revolutfers.model.Account;
import me.memleak.revolutfers.repository.AccountMapRepository;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

public class AccountServiceTest extends BaseServiceTest {

  private static final Long ACCOUNT_ID = 0L;

  private AccountService uut;
  private AccountMapRepository repository;

  @Override
  public void setUp() throws Exception {
    super.setUp();

    uut = injector.getInstance(AccountService.class);
    repository = injector.getInstance(AccountMapRepository.class);
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
    Account account = new Account(ACCOUNT_ID, BigDecimal.ONE);

    //when
    uut.update(account);

    //then
    verify(repository).update(eq(account));
  }

  @Override
  AbstractModule mockedGuiceModules() {
    return new AbstractModule() {
      @Override
      protected void configure() {
        bind(AccountMapRepository.class)
            .toInstance(mock(AccountMapRepository.class));
      }
    };
  }
}
