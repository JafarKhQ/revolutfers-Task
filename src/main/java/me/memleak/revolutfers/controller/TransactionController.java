package me.memleak.revolutfers.controller;

import io.javalin.Context;
import me.memleak.revolutfers.events.NewTransactionEvent;
import me.memleak.revolutfers.model.Transaction;
import me.memleak.revolutfers.model.TransactionRequest;
import me.memleak.revolutfers.service.TransactionService;
import org.eclipse.jetty.http.HttpStatus;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class TransactionController {

  private final TransactionService service;
  private final NewTransactionEvent transactionEvent;

  @Inject
  public TransactionController(TransactionService service, NewTransactionEvent transactionEvent) {
    this.service = service;
    this.transactionEvent = transactionEvent;
  }

  public void getAllTransactions(Context ctx) {
    ctx.json(service.getAll());
  }

  public void getTransaction(Context ctx) {
    long id = ctx.validatedPathParam("id").asLong()
        .check(it -> it >= 0, "Id cant be negative.")
        .getOrThrow();

    ctx.json(service.get(id));
  }

  public void createTransaction(Context ctx) {
    TransactionRequest transactionRequest = ctx.validatedBodyAsClass(TransactionRequest.class)
        .check(t -> t.getSourceAccount() >= 0, "Source Account cant be negative.")
        .check(t -> t.getDestinationAccount() >= 0, "Destination Account cant be negative.")
        .check(t -> t.getSourceAccount() != t.getDestinationAccount(), "Source and Destination Accounts cant be same.")
        .check(t -> t.getAmount() > 0.0, "Amount must be greater than zero.")
        .getOrThrow();

    Transaction transaction = service.create(transactionRequest);
    transactionEvent.onNewTransaction(transaction);

    ctx.json(transaction)
        .status(HttpStatus.ACCEPTED_202);
  }
}
