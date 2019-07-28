package me.memleak.revolutfers.service;

import me.memleak.revolutfers.exception.AccountNotFoundException;
import me.memleak.revolutfers.model.Account;
import me.memleak.revolutfers.model.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.text.MessageFormat;
import java.util.Queue;

import static java.util.Comparator.comparing;

@Singleton
public class TransactionProcessor {
  private static final Logger LOGGER = LoggerFactory.getLogger(TransactionProcessor.class);

  private final Queue<Transaction> queue;
  private final AccountService accountService;

  @Inject
  public TransactionProcessor(AccountService accountService, Queue<Transaction> queue) {
    this.queue = queue;
    this.accountService = accountService;
  }

  public Transaction processNext() {
    Transaction transaction = queue.poll();
    assert transaction != null; // shouldn't happen
    LOGGER.info("Start processing Transaction {}", transaction.toString());

    Account src = null, dest;
    try {
      src = accountService.get(transaction.getSourceId());
      dest = accountService.get(transaction.getDestinationId());
    } catch (AccountNotFoundException e) {
      String notFound = src == null ? "Source" : "Destination";
      LOGGER.info("The {} account is Not found.", notFound);

      transaction.setMessage(MessageFormat.format("The {0} Account is not found.", notFound));
      transaction.setStatus(Transaction.TransactionStatus.FAILED);
      return transaction;
    }

    try {
      accountService.lockAccounts(src, dest);
      if (src.getBalance().compareTo(transaction.getAmount()) < 0) {
        LOGGER.info("Insufficient fund.");
        transaction.setMessage("Insufficient fund.");
        transaction.setStatus(Transaction.TransactionStatus.FAILED);
      } else {
        LOGGER.info("Transferring fund.");
        src.setBalance(src.getBalance().subtract(transaction.getAmount()));
        dest.setBalance(dest.getBalance().add(transaction.getAmount()));

        accountService.update(src);
        accountService.update(dest);

        transaction.setStatus(Transaction.TransactionStatus.EXECUTED);
      }
    } finally {
      accountService.unlockAccounts(src, dest);
    }

    return transaction;
  }
}
