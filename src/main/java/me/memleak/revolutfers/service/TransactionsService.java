package me.memleak.revolutfers.service;

import me.memleak.revolutfers.events.TransactionEvent;
import me.memleak.revolutfers.model.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.Queue;
import java.util.concurrent.*;

@Singleton
public class TransactionsService implements TransactionEvent {
  private static final Logger LOGGER = LoggerFactory.getLogger(TransactionsService.class);

  private final Queue<Transaction> queue;
  private final ExecutorService executor;

  private final TransactionsConsumer transactionsConsumer;

  @Inject
  public TransactionsService(TransactionsConsumer transactionsConsumer, Queue<Transaction> queue,
                             @Named("transactions.thread.size") int nThreads) {
    this.queue = queue;
    this.executor = Executors.newFixedThreadPool(nThreads);
    this.transactionsConsumer = transactionsConsumer;
  }

  public void stop() {
    LOGGER.info("Stopping executor");
    executor.shutdown();
    try {
      executor.awaitTermination(1, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      // ignore it
    }
  }

  @Override
  public Future<Transaction> onNewTransaction(Transaction transaction) {
      LOGGER.info("The new Transaction added to the queue.");
      queue.add(transaction);
      return executor.submit(transactionsConsumer::consumeNext);
  }
}
