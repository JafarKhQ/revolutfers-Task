package me.memleak.revolutfers.service;

import com.google.inject.AbstractModule;
import me.memleak.revolutfers.exception.AccountNotFoundException;
import me.memleak.revolutfers.model.Account;
import me.memleak.revolutfers.repository.AccountMapRepository;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class AccountServiceTest extends BaseServiceTest {

  private static final Long ACCOUNT_ID = 0L;

  private AccountService service;
  private AccountMapRepository repository;

  @Override
  public void setUp() throws Exception {
    super.setUp();

    service = injector.getInstance(AccountService.class);
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

    Account result = service.create(1.00);

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

    List<Account> result = service.getAll();

    assertThat(result).isEqualTo(expected);
    verify(repository, only()).findAll();
  }

  @Test
  public void find() {
    Account expected = new Account(ACCOUNT_ID);
    when(repository.find(eq(ACCOUNT_ID))).thenReturn(Optional.of(expected));

    Account result = service.get(ACCOUNT_ID);

    assertThat(result).isEqualTo(expected);
    verify(repository, only()).find(eq(ACCOUNT_ID));
  }

  @Test(expected = AccountNotFoundException.class)
  public void find_accountNotFound() {
    when(repository.find(eq(ACCOUNT_ID))).thenReturn(Optional.empty());

    try {
      service.get(ACCOUNT_ID);
    } finally {
      verify(repository, only()).find(eq(ACCOUNT_ID));
    }
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
