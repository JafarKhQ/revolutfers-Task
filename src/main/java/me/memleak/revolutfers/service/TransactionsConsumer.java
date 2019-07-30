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
    Transaction transaction = null;
    try {
      synchronized (this) {
        /*
         * synchronized the part where polling then locking to make sure
         * no other Thread do Polling after and locking before.
         */
        transaction = queue.poll();
        assert transaction != null; // shouldn't happen
        LOGGER.debug("Polling & Locking the Transaction {}.", transaction);
        accountsService.lockAccounts(transaction.getSourceId(), transaction.getDestinationId());
      } // synchronized

      Account src = accountsService.get(transaction.getSourceId());
      Account dest = accountsService.get(transaction.getDestinationId());
      if (src.getBalance().compareTo(transaction.getAmount()) < 0) {
        LOGGER.info("Insufficient fund for {}.", transaction);
        throw new InsufficientFundException("Insufficient fund");
      } else {
        LOGGER.debug("Transferring the fund for {}.", transaction);
        src.setBalance(src.getBalance().subtract(transaction.getAmount()));
        dest.setBalance(dest.getBalance().add(transaction.getAmount()));

        accountsService.update(src);
        accountsService.update(dest);
        transaction.setStatus("Transaction Completed :)");
      }
    } finally {
      if (null != transaction) {
        LOGGER.debug("Unlocking for Transaction {}", transaction);
        accountsService.unlockAccounts(transaction.getSourceId(), transaction.getDestinationId());
      }
    }

    return transaction;
  }
}
