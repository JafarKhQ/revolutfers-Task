package me.memleak.revolutfers.service;

import me.memleak.revolutfers.exception.AccountNotFoundException;
import me.memleak.revolutfers.model.Account;
import me.memleak.revolutfers.model.ModelId;
import me.memleak.revolutfers.model.Transaction;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.text.MessageFormat;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.stream.Stream;

import static java.util.Comparator.comparing;

@Singleton
public class TransactionsExecutorService {
  private static final int PERIOD = 20;
  private static final int INITIAL_DELAY = 10;


  private final ScheduledExecutorService executor;
  private final Queue<Transaction> queue;

  private final AccountService accountService;
  private final TransactionService transactionService;

  @Inject
  public TransactionsExecutorService(AccountService accountService, TransactionService transactionService) {
    this.queue = new ConcurrentLinkedQueue<>();
    this.executor = Executors.newScheduledThreadPool(6);

    this.accountService = accountService;
    this.transactionService = transactionService;
  }

  public void start() {
    executor.scheduleAtFixedRate(transactionTask, INITIAL_DELAY, PERIOD, TimeUnit.SECONDS);
  }

  public void stop() {
    executor.shutdown();
  }

  private Runnable transactionTask = new Runnable() {
    @Override
    public void run() {
      if (queue.isEmpty()) return;

      Account src = null, dest = null;
      final Transaction transaction = queue.poll();
      try {
        src = accountService.get(transaction.getSourceId());
        dest = accountService.get(transaction.getDestinationId());
      } catch (AccountNotFoundException e) {
        String notFound = src == null ? "Source" : "Destination";

        transaction.setMessage(MessageFormat.format("The {0} Account is not found.", notFound));
        transaction.setStatus(Transaction.TransactionStatus.FAILED);
        transactionService.update(transaction);
        return;
      }

      final Stream<Lock> locks = lock(src, dest);
      try {
        if (src.getBalance().compareTo(transaction.getAmount()) < 0) {
          transaction.setMessage("Insufficient fund.");
          transaction.setStatus(Transaction.TransactionStatus.FAILED);
        } else {
          src.setBalance(src.getBalance().subtract(transaction.getAmount()));
          dest.setBalance(dest.getBalance().add(transaction.getAmount()));

          accountService.update(src);
          accountService.update(dest);

          transaction.setStatus(Transaction.TransactionStatus.EXECUTED);
        }

        transactionService.update(transaction);
      } finally {
        unlock(locks);
      }
    }
  };

  private Stream<Lock> lock(Account src, Account dest) {
    return Stream.of(src, dest)
        .sorted(comparing(ModelId::getId))
        .map(Account::getLock)
        .peek(Lock::lock);
  }

  private void unlock(Stream<Lock> locks) {
    locks.forEach(Lock::unlock);
  }
}
