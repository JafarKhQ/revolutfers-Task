package me.memleak.revolutfers.controller;

import io.javalin.http.Context;
import me.memleak.revolutfers.events.NewTransactionEvent;
import me.memleak.revolutfers.model.Transaction;
import me.memleak.revolutfers.controller.model.TransactionRequest;
import me.memleak.revolutfers.service.TransactionService;
import org.eclipse.jetty.http.HttpStatus;

import javax.inject.Inject;
import javax.inject.Singleton;

import static me.memleak.revolutfers.controller.model.ModelResponce.ok;

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
    ctx.json(ok(service.getAll()));
  }

  public void getTransaction(Context ctx) {
    long id = ctx.pathParam("id", Long.class)
        .check(it -> it >= 0, "Id cant be negative.")
        .get();

    ctx.json(ok(service.get(id)));
  }

  public void createTransaction(Context ctx) {
    TransactionRequest transactionRequest = ctx.bodyValidator(TransactionRequest.class)
        .check(t -> t.getSourceAccount() >= 0, "Source Account cant be negative.")
        .check(t -> t.getDestinationAccount() >= 0, "Destination Account cant be negative.")
        .check(t -> t.getSourceAccount() != t.getDestinationAccount(), "Source and Destination Accounts cant be same.")
        .check(t -> t.getAmount() > 0.0, "Amount must be greater than zero.")
        .get();

    Transaction transaction = service.create(transactionRequest);
    transactionEvent.onNewTransaction(transaction);

    ctx.json(ok(transaction))
        .status(HttpStatus.ACCEPTED_202);
  }
}
