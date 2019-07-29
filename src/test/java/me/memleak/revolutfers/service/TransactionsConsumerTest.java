package me.memleak.revolutfers.service;

import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import me.memleak.revolutfers.exception.AccountNotFoundException;
import me.memleak.revolutfers.exception.InsufficientFundException;
import me.memleak.revolutfers.model.Account;
import me.memleak.revolutfers.model.Transaction;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.util.Queue;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;
import static org.mockito.Mockito.*;

public class TransactionsConsumerTest extends BaseServiceTest {
  private static final long SRC_ACCOUNT_ID = 69;
  private static final long DST_ACCOUNT_ID = 89;
  private static final long NOT_FOUND_ACCOUNT_ID = 42;

  private Queue<Transaction> queue;
  private AccountsService accountsService;
  private TransactionsConsumer uut;

  @Override
  public void setUp() throws Exception {
    super.setUp();

    queue = injector.getInstance(new Key<Queue<Transaction>>() {
    });
    accountsService = injector.getInstance(AccountsService.class);
    uut = injector.getInstance(TransactionsConsumer.class);
  }

  @Override
  public void tearDown() throws Exception {
    verifyNoMoreInteractions(accountsService, queue);
  }

  @Test
  public void shouldFailedWhenSrcAccountNotFound() {
    // given
    Transaction transaction = new Transaction(NOT_FOUND_ACCOUNT_ID, DST_ACCOUNT_ID, BigDecimal.ZERO);
    when(queue.poll()).thenReturn(transaction);
    when(accountsService.get(eq(NOT_FOUND_ACCOUNT_ID))).thenThrow(AccountNotFoundException.class);

    // when
    try {
      uut.consumeNext();
      failBecauseExceptionWasNotThrown(AccountNotFoundException.class);
    } catch (AccountNotFoundException e) {
      //then
      verify(queue).poll();
      verify(accountsService).get(eq(NOT_FOUND_ACCOUNT_ID));
    }
  }

  @Test
  public void shouldFailedWhenDstAccountNotFound() {
    // given
    Transaction transaction = new Transaction(SRC_ACCOUNT_ID, NOT_FOUND_ACCOUNT_ID, BigDecimal.ZERO);
    when(queue.poll()).thenReturn(transaction);
    when(accountsService.get(eq(SRC_ACCOUNT_ID))).thenReturn(new Account());
    when(accountsService.get(eq(NOT_FOUND_ACCOUNT_ID))).thenThrow(AccountNotFoundException.class);

    try {
      // when
      uut.consumeNext();
      failBecauseExceptionWasNotThrown(AccountNotFoundException.class);
    } catch (AccountNotFoundException e) {
      //then
      verify(queue).poll();
      verify(accountsService).get(eq(SRC_ACCOUNT_ID));
      verify(accountsService).get(eq(NOT_FOUND_ACCOUNT_ID));
    }
  }

  @Test
  public void shouldFailedWhenInsufficientFund() {
    // given
    Account src = new Account(SRC_ACCOUNT_ID, BigDecimal.ONE),
        dest = new Account(DST_ACCOUNT_ID, BigDecimal.ZERO);
    Transaction transaction = new Transaction(SRC_ACCOUNT_ID, DST_ACCOUNT_ID, BigDecimal.TEN);
    when(queue.poll()).thenReturn(transaction);
    when(accountsService.get(eq(SRC_ACCOUNT_ID))).thenReturn(src);
    when(accountsService.get(eq(DST_ACCOUNT_ID))).thenReturn(dest);

    try {
      // when
      uut.consumeNext();
      failBecauseExceptionWasNotThrown(InsufficientFundException.class);
    } catch (InsufficientFundException e) {
      //then
      verify(queue).poll();
      verify(accountsService).lockAccounts(eq(src), eq(dest));
      verify(accountsService).get(eq(SRC_ACCOUNT_ID));
      verify(accountsService).get(eq(DST_ACCOUNT_ID));
      verify(accountsService).unlockAccounts(eq(src), eq(dest));
    }
  }

  @Test
  public void shouldExecuteAndUpdateAccounts() {
    // given
    Account src = new Account(SRC_ACCOUNT_ID, BigDecimal.ONE),
        dest = new Account(DST_ACCOUNT_ID, BigDecimal.ZERO);
    Transaction transaction = new Transaction(SRC_ACCOUNT_ID, DST_ACCOUNT_ID, BigDecimal.ONE);
    when(queue.poll()).thenReturn(transaction);
    when(accountsService.get(eq(SRC_ACCOUNT_ID))).thenReturn(src);
    when(accountsService.get(eq(DST_ACCOUNT_ID))).thenReturn(dest);

    // when
    Transaction result = uut.consumeNext();

    //then
    verify(queue).poll();
    verify(accountsService).lockAccounts(eq(src), eq(dest));
    verify(accountsService).get(eq(SRC_ACCOUNT_ID));
    verify(accountsService).get(eq(DST_ACCOUNT_ID));
    verify(accountsService).unlockAccounts(eq(src), eq(dest));

    ArgumentCaptor<Account> accountsArgument = ArgumentCaptor.forClass(Account.class);
    verify(accountsService, times(2)).update(accountsArgument.capture());

    assertThat(result.getAmount()).isEqualTo(BigDecimal.ONE);
    assertThat(result.getSourceId()).isEqualTo(SRC_ACCOUNT_ID);
    assertThat(result.getDestinationId()).isEqualTo(DST_ACCOUNT_ID);

    final Account srcAccount = accountsArgument.getAllValues().get(0);
    final Account destAccount = accountsArgument.getAllValues().get(1);
    assertThat(srcAccount.getId()).isEqualTo(SRC_ACCOUNT_ID);
    assertThat(srcAccount.getBalance()).isEqualTo(BigDecimal.ZERO);

    assertThat(destAccount.getId()).isEqualTo(DST_ACCOUNT_ID);
    assertThat(destAccount.getBalance()).isEqualTo(BigDecimal.ONE);
  }

  @Override
  AbstractModule mockedGuiceModules() {
    return new AbstractModule() {
      @Override
      protected void configure() {
        bind(new TypeLiteral<Queue<Transaction>>() {
        }).toInstance(mock(Queue.class));
        bind(AccountsService.class)
            .toInstance(mock(AccountsService.class));
      }
    };
  }
}