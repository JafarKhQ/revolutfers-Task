package me.memleak.revolutfers.controller;

import io.javalin.http.Context;
import me.memleak.revolutfers.controller.model.ModelResponce;
import me.memleak.revolutfers.controller.model.TransactionRequest;
import me.memleak.revolutfers.events.NewTransactionEvent;
import me.memleak.revolutfers.model.Transaction;
import me.memleak.revolutfers.util.TransactionFactory;
import org.eclipse.jetty.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static me.memleak.revolutfers.controller.model.ModelResponce.error;
import static me.memleak.revolutfers.controller.model.ModelResponce.ok;

@Singleton
public class TransactionController {
  private static final Logger LOGGER = LoggerFactory.getLogger(TransactionController.class);

  private final NewTransactionEvent transactionEvent;

  @Inject
  public TransactionController(NewTransactionEvent transactionEvent) {
    this.transactionEvent = transactionEvent;
  }

  public void createTransaction(Context ctx) {
    TransactionRequest transactionRequest = ctx.bodyValidator(TransactionRequest.class)
        .check(t -> t.getSourceAccount() >= 0, "Source Account cant be negative.")
        .check(t -> t.getDestinationAccount() >= 0, "Destination Account cant be negative.")
        .check(t -> t.getSourceAccount() != t.getDestinationAccount(), "Source and Destination Accounts cant be same.")
        .check(t -> t.getAmount() > 0.0, "Amount must be greater than zero.")
        .get();

    /*
     * For the sake of simplicity (and since all accounts are in memory so the Transaction will be executed quickly)
     * Im going to block the request until the Transaction get executed. :)
     */
    Future<Transaction> result = transactionEvent.onNewTransaction(TransactionFactory.from(transactionRequest));

    try {
      Transaction transaction = result.get();
      ctx.json(ok(transaction))
          .status(HttpStatus.OK_200);
    } catch (InterruptedException | ExecutionException e) {
      // todo: handle this better
      LOGGER.error("Error execution the transaction", e);
      ctx.json(ModelResponce.error("Error execution the transaction"))
          .status(HttpStatus.INTERNAL_SERVER_ERROR_500);
    }
  }
}
