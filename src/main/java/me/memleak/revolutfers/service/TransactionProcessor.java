package me.memleak.revolutfers.service;

import me.memleak.revolutfers.exception.InsufficientFundException;
import me.memleak.revolutfers.model.Account;
import me.memleak.revolutfers.model.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Queue;

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

    Account src = accountService.get(transaction.getSourceId());
    Account dest = accountService.get(transaction.getDestinationId());
    try {
      accountService.lockAccounts(src, dest);
      if (src.getBalance().compareTo(transaction.getAmount()) < 0) {
        LOGGER.info("Insufficient fund.");
        throw new InsufficientFundException("Insufficient fund");
      } else {
        LOGGER.info("Transferring fund.");
        src.setBalance(src.getBalance().subtract(transaction.getAmount()));
        dest.setBalance(dest.getBalance().add(transaction.getAmount()));

        accountService.update(src);
        accountService.update(dest);
      }
    } finally {
      accountService.unlockAccounts(src, dest);
    }

    return transaction;
  }
}
