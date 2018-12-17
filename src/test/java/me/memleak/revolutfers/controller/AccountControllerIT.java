package me.memleak.revolutfers.controller;

import me.memleak.revolutfers.exception.AccountNotFoundException;
import me.memleak.revolutfers.model.Account;
import me.memleak.revolutfers.service.AccountService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class AccountControllerIT extends BaseControllerIT {
  private static final Long ACCOUNT_ID = 0L;

  private AccountService service;

  @Before
  public void setUp() {
    service = injector.getInstance(AccountService.class);
  }

  @After
  public void tearDown() {
    verifyNoMoreInteractions(service);
    reset(service);
  }

  @Test
  public void createAccount() throws Exception {
    double amount = 1.000;
    Account expected = new Account(ACCOUNT_ID, BigDecimal.ONE);
    when(service.create(anyDouble())).thenReturn(expected);

    Account result = post("accounts", amount, Account.class);

    assertThat(result).isEqualTo(expected);
    verify(service, only()).create(eq(amount));
  }

  @Test
  public void createAccount_invalidAmount() throws Exception {
    double amount = -1;

    String result = post("accounts", amount, String.class);

    assertThat(result).endsWith("Account balance cant be less than ZERO.");
  }

  @Test
  public void getAllAccounts() throws Exception {
    List<Account> expected = new ArrayList<>();
    when(service.getAll()).thenReturn(expected);

    Account[] result = get("accounts", Account[].class);

    assertThat(result).hasSameElementsAs(expected);
    verify(service, only()).getAll();
  }

  @Test
  public void getAccount() throws Exception {
    Account expected = new Account(ACCOUNT_ID);
    when(service.get(anyLong())).thenReturn(expected);

    Account result = get("accounts/" + ACCOUNT_ID, Account.class);

    assertThat(result).isEqualTo(expected);
    verify(service, only()).get(eq(ACCOUNT_ID));
  }

  @Test
  public void getAccount_invalidId() throws Exception {
    String result = get("accounts/-5", String.class);

    assertThat(result).endsWith("Id cant be negative.");
  }

  @Test
  public void getAccount_notFound() throws Exception {
    when(service.get(anyLong())).thenThrow(new AccountNotFoundException("bla bla"));

    String result = get("accounts/" + ACCOUNT_ID, String.class);

    assertThat(result).endsWith("bla bla");
    verify(service, only()).get(eq(ACCOUNT_ID));
  }
}