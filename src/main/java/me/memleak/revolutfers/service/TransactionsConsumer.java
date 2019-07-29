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
public class TransactionsConsumer {
  private static final Logger LOGGER = LoggerFactory.getLogger(TransactionsConsumer.class);

  private final Queue<Transaction> queue;
  private final AccountsService accountsService;

  @Inject
  public TransactionsConsumer(AccountsService accountsService, Queue<Transaction> queue) {
    this.queue = queue;
    this.accountsService = accountsService;
  }

  public Transaction consumeNext() {
    Transaction transaction = queue.poll();
    assert transaction != null; // shouldn't happen
    LOGGER.info("Start processing Transaction {}", transaction.toString());

    Account src = accountsService.get(transaction.getSourceId());
    Account dest = accountsService.get(transaction.getDestinationId());
    try {
      accountsService.lockAccounts(src, dest);
      if (src.getBalance().compareTo(transaction.getAmount()) < 0) {
        LOGGER.info("Insufficient fund.");
        throw new InsufficientFundException("Insufficient fund");
      } else {
        LOGGER.info("Transferring fund.");
        src.setBalance(src.getBalance().subtract(transaction.getAmount()));
        dest.setBalance(dest.getBalance().add(transaction.getAmount()));

        accountsService.update(src);
        accountsService.update(dest);
      }
    } finally {
      accountsService.unlockAccounts(src, dest);
    }

    return transaction;
  }
}
