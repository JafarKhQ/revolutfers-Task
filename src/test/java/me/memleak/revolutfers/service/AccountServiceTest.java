package me.memleak.revolutfers.service;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import me.memleak.revolutfers.exception.AccountNotFoundException;
import me.memleak.revolutfers.model.Account;
import me.memleak.revolutfers.repository.AccountMapRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;


public class AccountServiceTest {

  private static final Long ACCOUNT_ID = 0L;

  private Injector injector;
  private AccountService service;
  private AccountMapRepository repository;

  @Before
  public void setUp() throws Exception {
    injector = Guice.createInjector(mockedGuiceModules());

    service = injector.getInstance(AccountService.class);
    repository = injector.getInstance(AccountMapRepository.class);
  }

  @After
  public void tearDown() throws Exception {
    verifyNoMoreInteractions(repository);
  }

  @Test
  public void toBankingBalance() {
    BigDecimal result = service.toBankingBalance.apply(1.0000);
    assertThat(result).isEqualByComparingTo(BigDecimal.ONE);

    result = service.toBankingBalance.apply(0.69);
    assertThat(result).isEqualByComparingTo(BigDecimal.valueOf(0.69));

    result = service.toBankingBalance.apply(1.688);
    assertThat(result).isEqualByComparingTo(BigDecimal.valueOf(1.69));

    result = service.toBankingBalance.apply(2.683);
    assertThat(result).isEqualByComparingTo(BigDecimal.valueOf(2.69));
  }

  @Test
  public void create() {
    Account expected = new Account(ACCOUNT_ID, BigDecimal.ONE);
    when(repository.create(any(BigDecimal.class))).thenReturn(expected);

    Account result = service.create(1.00);

    assertThat(result).isEqualTo(expected);
    verify(repository, only()).create(any(BigDecimal.class));
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