package me.memleak.revolutfers.controller;

import me.memleak.revolutfers.controller.model.TransactionRequest;
import me.memleak.revolutfers.events.TransactionEvent;
import me.memleak.revolutfers.exception.AccountNotFoundException;
import me.memleak.revolutfers.exception.InsufficientFundException;
import me.memleak.revolutfers.model.Transaction;
import me.memleak.revolutfers.service.AccountsService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static java.util.concurrent.CompletableFuture.failedFuture;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class TransactionsControllerTest extends BaseControllerTest {
  private static final long SRC_ACCOUNT_ID = 1;
  private static final long DEST_ACCOUNT_ID = 2;
  private static final long INVALID_ACCOUNT_ID = -1;

  private TransactionEvent event;
  private AccountsService accountsService;

  @Before
  public void setUp() {
    event = injector.getInstance(TransactionEvent.class);
    accountsService = injector.getInstance(AccountsService.class);
  }

  @After
  public void tearDown() {
    verifyNoMoreInteractions(event, accountsService);
    reset(event, accountsService);
  }

  @Test
  public void createTransaction() throws Exception {
    //given
    TransactionRequest request = new TransactionRequest();
    request.setSourceAccount(SRC_ACCOUNT_ID);
    request.setDestinationAccount(DEST_ACCOUNT_ID);
    request.setAmount(10);

    Transaction expected = new Transaction(SRC_ACCOUNT_ID, DEST_ACCOUNT_ID, BigDecimal.TEN);
    when(event.onNewTransaction(any(Transaction.class))).
        thenReturn(completedFuture(expected));

    //when
    Transaction result = post("transactions", request, Transaction.class).getBody();

    //then
    verify(accountsService).get(eq(SRC_ACCOUNT_ID));
    verify(accountsService).get(eq(DEST_ACCOUNT_ID));
    verify(event, only()).onNewTransaction(eq(expected));

    assertThat(result).isEqualTo(expected);
  }

  @Test
  public void createTransactionInvalidSource() throws Exception {
    //given
    TransactionRequest request = new TransactionRequest();
    request.setSourceAccount(INVALID_ACCOUNT_ID);
    request.setDestinationAccount(DEST_ACCOUNT_ID);
    request.setAmount(1);

    //when
    String result = post("transactions", request, Object.class).getMessage();

    //then
    assertThat(result).endsWith("Source Account cant be negative.");
  }

  @Test
  public void createTransactionInvalidDestination() throws Exception {
    //given
    TransactionRequest request = new TransactionRequest();
    request.setSourceAccount(SRC_ACCOUNT_ID);
    request.setDestinationAccount(INVALID_ACCOUNT_ID);
    request.setAmount(1);

    //when
    String result = post("transactions", request, Object.class).getMessage();

    //then
    assertThat(result).endsWith("Destination Account cant be negative.");
  }

  @Test
  public void createTransactionSameSourceDestination() throws Exception {
    //given
    TransactionRequest request = new TransactionRequest();
    request.setSourceAccount(SRC_ACCOUNT_ID);
    request.setDestinationAccount(SRC_ACCOUNT_ID);
    request.setAmount(1);

    //when
    String result = post("transactions", request, Object.class).getMessage();

    //then
    assertThat(result).endsWith("Source and Destination Accounts cant be same.");
  }

  @Test
  public void createTransactionInvalidAmount() throws Exception {
    //given
    TransactionRequest request = new TransactionRequest();
    request.setSourceAccount(SRC_ACCOUNT_ID);
    request.setDestinationAccount(DEST_ACCOUNT_ID);
    request.setAmount(-1);

    //when
    String result = post("transactions", request, Object.class).getMessage();

    //then
    assertThat(result).endsWith("Amount must be greater than zero.");
  }

  @Test
  public void createTransactionSrcAccountNotFound() throws Exception {
    //given
    TransactionRequest request = new TransactionRequest();
    request.setSourceAccount(SRC_ACCOUNT_ID);
    request.setDestinationAccount(DEST_ACCOUNT_ID);
    request.setAmount(1);

    when(accountsService.get(eq(SRC_ACCOUNT_ID))).
        thenThrow(new AccountNotFoundException("Account not found."));

    //when
    String result = post("transactions", request, String.class).getMessage();

    //then
    verify(accountsService, only()).get(eq(SRC_ACCOUNT_ID));
    assertThat(result).containsIgnoringCase("Account not found");
  }

  @Test
  public void createTransactionDestAccountNotFound() throws Exception {
    //given
    TransactionRequest request = new TransactionRequest();
    request.setSourceAccount(SRC_ACCOUNT_ID);
    request.setDestinationAccount(DEST_ACCOUNT_ID);
    request.setAmount(1);

    when(accountsService.get(eq(DEST_ACCOUNT_ID))).
        thenThrow(new AccountNotFoundException("Account not found."));

    //when
    String result = post("transactions", request, String.class).getMessage();

    //then
    verify(accountsService).get(eq(SRC_ACCOUNT_ID));
    verify(accountsService).get(eq(DEST_ACCOUNT_ID));

    assertThat(result).containsIgnoringCase("Account not found");
  }

  @Test
  public void createTransactionInsufficientFund() throws Exception {
    //given
    TransactionRequest request = new TransactionRequest();
    request.setSourceAccount(SRC_ACCOUNT_ID);
    request.setDestinationAccount(DEST_ACCOUNT_ID);
    request.setAmount(1);
    Transaction transaction = new Transaction(SRC_ACCOUNT_ID, DEST_ACCOUNT_ID, BigDecimal.ONE);

    when(event.onNewTransaction(any(Transaction.class))).
        thenReturn(failedFuture(new InsufficientFundException("Insufficient Fund.")));

    //when
    String result = post("transactions", request, String.class).getMessage();

    //then
    verify(accountsService).get(eq(SRC_ACCOUNT_ID));
    verify(accountsService).get(eq(DEST_ACCOUNT_ID));
    verify(event, only()).onNewTransaction(eq(transaction));

    assertThat(result).containsIgnoringCase("insufficient fund");
  }
}
