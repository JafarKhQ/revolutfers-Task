package me.memleak.revolutfers.controller;

import me.memleak.revolutfers.controller.model.AccountRequest;
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
    reset(service);
  }

  @After
  public void tearDown() {
    verifyNoMoreInteractions(service);
  }

  @Test
  public void createAccount() throws Exception {
    Account expected = new Account(ACCOUNT_ID, BigDecimal.ONE);
    when(service.create(any(AccountRequest.class))).thenReturn(expected);

    AccountRequest request = new AccountRequest();
    request.setBalance(1.000);
    Account result = post("accounts", request, Account.class).getBody();

    assertThat(result).isEqualTo(expected);
    verify(service, only()).create(eq(request));
  }

  @Test
  public void createAccount_invalidAmount() throws Exception {
    AccountRequest request = new AccountRequest();
    request.setBalance(-1);
    String result = post("accounts", request, Object.class).getMessage();

    assertThat(result).endsWith("Account balance cant be less than ZERO.");
  }

  @Test
  public void getAllAccounts() throws Exception {
    List<Account> expected = new ArrayList<>();
    when(service.getAll()).thenReturn(expected);

    Account[] result = get("accounts", Account[].class).getBody();

    assertThat(result).hasSameElementsAs(expected);
    verify(service, only()).getAll();
  }

  @Test
  public void getAccount() throws Exception {
    Account expected = new Account(ACCOUNT_ID);
    when(service.get(anyLong())).thenReturn(expected);

    Account result = get("accounts/" + ACCOUNT_ID, Account.class).getBody();

    assertThat(result).isEqualTo(expected);
    verify(service, only()).get(eq(ACCOUNT_ID));
  }

  @Test
  public void getAccount_invalidId() throws Exception {
    String result = get("accounts/-5", Object.class).getMessage();

    assertThat(result).endsWith("Id cant be negative.");
  }

  @Test
  public void getAccount_notFound() throws Exception {
    when(service.get(anyLong())).thenThrow(new AccountNotFoundException("bla bla"));

    String result = get("accounts/" + ACCOUNT_ID, Object.class).getMessage();

    assertThat(result).endsWith("bla bla");
    verify(service, only()).get(eq(ACCOUNT_ID));
  }
}
