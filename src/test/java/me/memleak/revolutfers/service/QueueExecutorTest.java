package me.memleak.revolutfers.service;

import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;
import me.memleak.revolutfers.model.Transaction;
import org.junit.Test;
import org.mockito.InOrder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Queue;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;
import static org.mockito.Mockito.*;

public class QueueExecutorTest extends BaseServiceTest {

  private QueueExecutor uut;
  private Queue<Transaction> queue;
  private TransactionProcessor processor;

  @Override
  public void setUp() throws Exception {
    super.setUp();

    uut = injector.getInstance(QueueExecutor.class);
    queue = injector.getInstance(new Key<Queue<Transaction>>() {});
    processor = injector.getInstance(TransactionProcessor.class);
  }

  @Override
  public void tearDown() throws Exception {
    verifyNoMoreInteractions(processor, queue);
  }

  @Test
  public void transactionsProcessedInOrder() throws Exception {
    // given
    final int nTransactions = 5;
    InOrder inOrder = inOrder(queue);
    List<Transaction> transactions = IntStream.range(0, nTransactions)
        .mapToObj(n -> new Transaction(n + 1, n + 2, BigDecimal.valueOf(n)))
        .collect(toList());

    // when
    transactions.forEach(uut::onNewTransaction);
    uut.stop();

    // then
    verify(processor, times(nTransactions)).processNext();
    transactions.forEach(t -> inOrder.verify(queue).add(eq(t)));
  }

  @Override
  AbstractModule mockedGuiceModules() {
    return new AbstractModule() {
      @Override
      protected void configure() {
        bind(Integer.class)
            .annotatedWith(Names.named("transactions.thread.size"))
            .toInstance(2);
        bind(new TypeLiteral<Queue<Transaction>>() {
        }).toInstance(mock(Queue.class));
        bind(TransactionProcessor.class)
            .toInstance(mock(TransactionProcessor.class));
      }
    };
  }
}