package me.memleak.revolutfers.service;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import me.memleak.revolutfers.model.Transaction;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Queue;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.mockito.Mockito.*;

public class QueueExecutorTest extends BaseServiceTest {

  private QueueExecutor executor;
  private TransactionProcessor processor;

  @Override
  public void setUp() throws Exception {
    super.setUp();

    executor = injector.getInstance(QueueExecutor.class);
    processor = injector.getInstance(TransactionProcessor.class);
  }

  @Override
  public void tearDown() throws Exception {
    verifyNoMoreInteractions(processor);
  }

  @Test
  public void transactionsProcessedInOrder() throws Exception {
    List<Transaction> transactions = Stream.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9)
        .map(n -> new Transaction(0, 0, BigDecimal.ONE))
        .collect(toList());

    transactions.forEach(executor::onNewTransaction);

    // todo: remove sleep and correctly wait until all threads finished
    Thread.sleep(100);

    verify(processor, times(10)).processNext();
  }

  @Override
  AbstractModule mockedGuiceModules() {
    return new AbstractModule() {
      @Override
      protected void configure() {
        bind(new TypeLiteral<Queue<Transaction>>() {
        }).toInstance(mock(Queue.class));
        bind(TransactionProcessor.class)
            .toInstance(mock(TransactionProcessor.class));
      }
    };
  }
}