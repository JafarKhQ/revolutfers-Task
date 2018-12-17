package me.memleak.revolutfers.service;

import me.memleak.revolutfers.events.NewTransactionEvent;
import me.memleak.revolutfers.model.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Queue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Singleton
public class QueueExecutor implements NewTransactionEvent {
  private static final Logger LOGGER = LoggerFactory.getLogger(QueueExecutor.class);

  private static final int PERIOD = 20;
  private static final int INITIAL_DELAY = 10;

  private final Queue<Transaction> queue;
  private final ScheduledExecutorService executor;

  private final TransactionProcessor transactionProcessor;

  @Inject
  public QueueExecutor(TransactionProcessor transactionProcessor) {
    this.queue = new LinkedBlockingQueue<>();
//    this.queue = new ConcurrentLinkedQueue<>();
    this.executor = Executors.newScheduledThreadPool(6);

    this.transactionProcessor = transactionProcessor;
  }

  public void start() {
    LOGGER.info("Starting executor");
    executor.scheduleAtFixedRate(transactionTask, INITIAL_DELAY, PERIOD, TimeUnit.SECONDS);
  }

  public void stop() {
    LOGGER.info("Stopping executor");
    executor.shutdown();
  }

  private Runnable transactionTask = new Runnable() {
    @Override
    public void run() {
      if (queue.isEmpty()) {
        LOGGER.info("Thread {} empty queue.", Thread.currentThread().getName());
        return;
      }

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
    }
  }
}
