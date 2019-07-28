package me.memleak.revolutfers.controller;

import me.memleak.revolutfers.controller.model.TransactionRequest;
import me.memleak.revolutfers.events.NewTransactionEvent;
import me.memleak.revolutfers.model.Transaction;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class TransactionControllerIT extends BaseControllerIT {

  private NewTransactionEvent event;

  @Before
  public void setUp() {
    event = injector.getInstance(NewTransactionEvent.class);
  }

  @After
  public void tearDown() {
    verifyNoMoreInteractions(event);
  }

  @Test
  public void createTransaction() throws Exception {
    TransactionRequest request = new TransactionRequest();
    request.setSourceAccount(0);
    request.setDestinationAccount(1);
    request.setAmount(1);

    Transaction expected = new Transaction(0, 1, BigDecimal.ONE);
    when(event.onNewTransaction(any(Transaction.class))).
        thenReturn(CompletableFuture.completedFuture(expected));

    Transaction result = post("transactions", request, Transaction.class).getBody();

    verify(event, only()).onNewTransaction(eq(expected));
    assertThat(result).isEqualTo(expected);
  }

  @Test
  public void createTransaction_invalidSource() throws Exception {
    TransactionRequest request = new TransactionRequest();
    request.setSourceAccount(-1);
    request.setDestinationAccount(1);
    request.setAmount(1);

    String result = post("transactions", request, Object.class).getMessage();


    assertThat(result).endsWith("Source Account cant be negative.");
  }

  @Test
  public void createTransaction_invalidDestination() throws Exception {
    TransactionRequest request = new TransactionRequest();
    request.setSourceAccount(0);
    request.setDestinationAccount(-1);
    request.setAmount(1);

    String result = post("transactions", request, Object.class).getMessage();


    assertThat(result).endsWith("Destination Account cant be negative.");
  }

  @Test
  public void createTransaction_sameSourceDestination() throws Exception {
    TransactionRequest request = new TransactionRequest();
    request.setSourceAccount(0);
    request.setDestinationAccount(0);
    request.setAmount(1);

    String result = post("transactions", request, Object.class).getMessage();


    assertThat(result).endsWith("Source and Destination Accounts cant be same.");
  }

  @Test
  public void createTransaction_invalidAmount() throws Exception {
    TransactionRequest request = new TransactionRequest();
    request.setSourceAccount(0);
    request.setDestinationAccount(1);
    request.setAmount(-1);

    String result = post("transactions", request, Object.class).getMessage();


    assertThat(result).endsWith("Amount must be greater than zero.");
  }
}
