package me.memleak.revolutfers.controller;

import me.memleak.revolutfers.controller.model.TransactionRequest;
import me.memleak.revolutfers.events.TransactionEvent;
import me.memleak.revolutfers.exception.AccountNotFoundException;
import me.memleak.revolutfers.exception.InsufficientFundException;
import me.memleak.revolutfers.model.Transaction;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;

import static java.util.concurrent.CompletableFuture.failedFuture;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class TransactionsControllerTest extends BaseControllerTest {

  private TransactionEvent event;

  @Before
  public void setUp() {
    event = injector.getInstance(TransactionEvent.class);
  }

  @After
  public void tearDown() {
    verifyNoMoreInteractions(event);
    reset(event);
  }

  @Test
  public void createTransaction() throws Exception {
    //given
    TransactionRequest request = new TransactionRequest();
    request.setSourceAccount(0);
    request.setDestinationAccount(1);
    request.setAmount(1);

    Transaction expected = new Transaction(0, 1, BigDecimal.ONE);
    when(event.onNewTransaction(any(Transaction.class))).
        thenReturn(CompletableFuture.completedFuture(expected));

    //when
    Transaction result = post("transactions", request, Transaction.class).getBody();

    //then
    verify(event, only()).onNewTransaction(eq(expected));
    assertThat(result).isEqualTo(expected);
  }

  @Test
  public void createTransactionInvalidSource() throws Exception {
    //given
    TransactionRequest request = new TransactionRequest();
    request.setSourceAccount(-1);
    request.setDestinationAccount(1);
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
    request.setSourceAccount(0);
    request.setDestinationAccount(-1);
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
    request.setSourceAccount(0);
    request.setDestinationAccount(0);
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
    request.setSourceAccount(0);
    request.setDestinationAccount(1);
    request.setAmount(-1);

    //when
    String result = post("transactions", request, Object.class).getMessage();

    //then
    assertThat(result).endsWith("Amount must be greater than zero.");
  }

  @Test
  public void createTransactionAccountNotFound() throws Exception {
    //given
    TransactionRequest request = new TransactionRequest();
    request.setSourceAccount(0);
    request.setDestinationAccount(1);
    request.setAmount(1);
    Transaction transaction = new Transaction(0, 1, BigDecimal.ONE);

    when(event.onNewTransaction(any(Transaction.class))).
        thenReturn(failedFuture(new AccountNotFoundException("Account not found.")));

    //when
    String result = post("transactions", request, String.class).getMessage();

    //then
    verify(event, only()).onNewTransaction(eq(transaction));
    assertThat(result).containsIgnoringCase("Account not found");
  }

  @Test
  public void createTransactionInsufficientFund() throws Exception {
    //given
    TransactionRequest request = new TransactionRequest();
    request.setSourceAccount(0);
    request.setDestinationAccount(1);
    request.setAmount(1);
    Transaction transaction = new Transaction(0, 1, BigDecimal.ONE);

    when(event.onNewTransaction(any(Transaction.class))).
        thenReturn(failedFuture(new InsufficientFundException("Insufficient Fund.")));

    //when
    String result = post("transactions", request, String.class).getMessage();

    //then
    verify(event, only()).onNewTransaction(eq(transaction));
    assertThat(result).containsIgnoringCase("insufficient fund");
  }
}
