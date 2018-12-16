package me.memleak.revolutfers.controller;

import me.memleak.revolutfers.exception.TransactionNotFoundException;
import me.memleak.revolutfers.model.Transaction;
import me.memleak.revolutfers.model.TransactionRequest;
import me.memleak.revolutfers.service.TransactionService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class TransactionControllerIT extends BaseControllerIT {
  private static final Long TRANSACTION_ID = 0L;

  private TransactionService service;

  @Before
  public void setUp() {
    service = injector.getInstance(TransactionService.class);
  }

  @After
  public void tearDown() {
    verifyNoMoreInteractions(service);
    reset(service);
  }

  @Test
  public void createTransaction() throws Exception {
    TransactionRequest request = new TransactionRequest();
    request.setSourceAccount(0);
    request.setDestinationAccount(1);
    request.setAmount(1);

    Transaction expected = new Transaction(TRANSACTION_ID, 0, 1, BigDecimal.ONE);
    when(service.create(any(TransactionRequest.class))).thenReturn(expected);

    Transaction result = post("transactions", request, Transaction.class);

    verify(service, only()).create(eq(request));
    assertThat(result).isEqualTo(expected);
  }

  @Test
  public void createTransaction_invalidSource() throws Exception {
    TransactionRequest request = new TransactionRequest();
    request.setSourceAccount(-1);
    request.setDestinationAccount(1);
    request.setAmount(1);

    String result = post("transactions", request, String.class);


    assertThat(result).endsWith("Source Account cant be negative.");
  }

  @Test
  public void createTransaction_invalidDestination() throws Exception {
    TransactionRequest request = new TransactionRequest();
    request.setSourceAccount(0);
    request.setDestinationAccount(-1);
    request.setAmount(1);

    String result = post("transactions", request, String.class);


    assertThat(result).endsWith("Destination Account cant be negative.");
  }

  @Test
  public void createTransaction_sameSourceDestination() throws Exception {
    TransactionRequest request = new TransactionRequest();
    request.setSourceAccount(0);
    request.setDestinationAccount(0);
    request.setAmount(1);

    String result = post("transactions", request, String.class);


    assertThat(result).endsWith("Source and Destination Accounts cant be same.");
  }

  @Test
  public void createTransaction_invalidAmount() throws Exception {
    TransactionRequest request = new TransactionRequest();
    request.setSourceAccount(0);
    request.setDestinationAccount(1);
    request.setAmount(-1);

    String result = post("transactions", request, String.class);


    assertThat(result).endsWith("Amount must be greater than zero.");
  }

  @Test
  public void getAllTransactions() throws Exception {
    List<Transaction> expected = new ArrayList<>();
    when(service.getAll()).thenReturn(expected);

    Transaction[] result = get("transactions", Transaction[].class);

    assertThat(result).hasSameElementsAs(expected);
    verify(service, only()).getAll();
  }

  @Test
  public void getTransaction() throws Exception {
    Transaction expected = new Transaction(TRANSACTION_ID, 0, 1, BigDecimal.ONE);
    when(service.get(anyLong())).thenReturn(expected);

    Transaction result = get("transactions/" + TRANSACTION_ID, Transaction.class);

    assertThat(result).isEqualTo(expected);
    verify(service, only()).get(eq(TRANSACTION_ID));
  }

  @Test
  public void getTransaction_invalidId() throws Exception {
    String result = get("transactions/-5", String.class);

    assertThat(result).endsWith("Id cant be negative.");
  }

  @Test
  public void getTransaction_notFound() throws Exception {
    when(service.get(anyLong())).thenThrow(new TransactionNotFoundException("bla bla"));

    String result = get("transactions/" + TRANSACTION_ID, String.class);

    assertThat(result).endsWith("bla bla");
    verify(service, only()).get(eq(TRANSACTION_ID));
  }
}
