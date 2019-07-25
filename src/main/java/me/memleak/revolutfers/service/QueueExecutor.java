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
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Singleton
public class QueueExecutor implements NewTransactionEvent {
  private static final Logger LOGGER = LoggerFactory.getLogger(QueueExecutor.class);

  private final Queue<Transaction> queue;
  private final ExecutorService executor;

  private final TransactionProcessor transactionProcessor;

  @Inject
  public QueueExecutor(TransactionProcessor transactionProcessor, Queue<Transaction> queue) {
    this.queue = queue;
    // cant find an easy way to implement a multi threads processor.
    this.executor = Executors.newFixedThreadPool(4);

    this.transactionProcessor = transactionProcessor;
  }

  public void stop() {
    LOGGER.info("Stopping executor");
    executor.shutdown();
  }

  private Runnable transactionTask = new Runnable() {
    @Override
    public void run() {
      transactionProcessor.processNext();
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
