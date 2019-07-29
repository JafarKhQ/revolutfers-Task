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
    //given
    Account expected = new Account(ACCOUNT_ID, BigDecimal.ONE);
    when(service.create(any(AccountRequest.class))).thenReturn(expected);
    AccountRequest request = new AccountRequest();
    request.setBalance(1.000);

    //when
    Account result = post("accounts", request, Account.class).getBody();

    //then
    verify(service, only()).create(eq(request));
    assertThat(result).isEqualTo(expected);
  }

  @Test
  public void createAccountInvalidAmount() throws Exception {
    //given
    AccountRequest request = new AccountRequest();
    request.setBalance(-1);

    //when
    String result = post("accounts", request, Object.class).getMessage();

    //then
    assertThat(result).containsIgnoringCase("account balance cant be less than ZERO");
  }

  @Test
  public void getAllAccounts() throws Exception {
    //given
    List<Account> expected = new ArrayList<>();
    when(service.getAll()).thenReturn(expected);

    //when
    Account[] result = get("accounts", Account[].class).getBody();

    //then
    verify(service, only()).getAll();
    assertThat(result).hasSameElementsAs(expected);
  }

  @Test
  public void getAccount() throws Exception {
    //given
    Account expected = new Account(ACCOUNT_ID);
    when(service.get(anyLong())).thenReturn(expected);

    //when
    Account result = get("accounts/" + ACCOUNT_ID, Account.class).getBody();

    //then
    verify(service, only()).get(eq(ACCOUNT_ID));
    assertThat(result).isEqualTo(expected);
  }

  @Test
  public void getAccountInvalidId() throws Exception {
    //given

    //when
    String result = get("accounts/-5", Object.class).getMessage();

    //then
    assertThat(result).containsIgnoringCase("Id cant be negative");
  }

  @Test
  public void getAccountNotFoundAccount() throws Exception {
    //given
    when(service.get(anyLong())).thenThrow(new AccountNotFoundException("Account not found."));

    //when
    String result = get("accounts/" + ACCOUNT_ID, Object.class).getMessage();

    //then
    verify(service, only()).get(eq(ACCOUNT_ID));
    assertThat(result).containsIgnoringCase("account not found");
  }
}
