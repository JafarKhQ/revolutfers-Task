package me.memleak.revolutfers.service;

import me.memleak.revolutfers.events.NewTransactionEvent;
import me.memleak.revolutfers.model.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Singleton
public class QueueExecutor implements NewTransactionEvent {
  private static final Logger LOGGER = LoggerFactory.getLogger(QueueExecutor.class);

  private final Queue<Transaction> queue;
  private final ExecutorService executor;

  private final TransactionProcessor transactionProcessor;

  @Inject
  public QueueExecutor(TransactionProcessor transactionProcessor) {
    this.queue = new ConcurrentLinkedQueue<>();
    this.executor = Executors.newFixedThreadPool(6);

    this.transactionProcessor = transactionProcessor;
  }

  public void stop() {
    LOGGER.info("Stopping executor");
    executor.shutdown();
  }

  private Runnable transactionTask = new Runnable() {
    @Override
    public void run() {
      Transaction transaction = queue.poll();
      LOGGER.info("Thread {} processing Transaction {}.", Thread.currentThread().getName(), transaction.getId());
      transactionProcessor.process(transaction);
    }
  };

  @Override
  public void onNewTransaction(Transaction transaction) {
    if (transaction != null) {
      LOGGER.info("New Transaction {} added to the queue.", transaction.getId());
      queue.add(transaction);
      executor.execute(transactionTask);
    }
  }
}
