package me.memleak.revolutfers.service;

import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import me.memleak.revolutfers.exception.AccountNotFoundException;
import me.memleak.revolutfers.model.Account;
import me.memleak.revolutfers.model.Transaction;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.util.Queue;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class TransactionProcessorTest extends BaseServiceTest {
  private static final long SRC_ACCOUNT_ID = 69;
  private static final long DST_ACCOUNT_ID = 89;
  private static final long NOT_FOUND_ACCOUNT_ID = 42;

  private Queue<Transaction> queue;
  private AccountService accountService;
  private TransactionProcessor uut;

  @Override
  public void setUp() throws Exception {
    super.setUp();

    queue = injector.getInstance(new Key<Queue<Transaction>>() {
    });
    accountService = injector.getInstance(AccountService.class);
    uut = injector.getInstance(TransactionProcessor.class);
  }

  @Override
  public void tearDown() throws Exception {
    verifyNoMoreInteractions(accountService, queue);
  }

  @Test
  public void shouldFailedWhenSrcAccountNotFound() {
    // given
    Transaction transaction = new Transaction(NOT_FOUND_ACCOUNT_ID, DST_ACCOUNT_ID, BigDecimal.ZERO);
    when(queue.poll()).thenReturn(transaction);
    when(accountService.get(eq(NOT_FOUND_ACCOUNT_ID))).thenThrow(AccountNotFoundException.class);

    // when
    Transaction result = uut.processNext();

    //then
    verify(queue).poll();
    verify(accountService).get(eq(NOT_FOUND_ACCOUNT_ID));

    assertThat(result.getAmount()).isEqualTo(BigDecimal.ZERO);
    assertThat(result.getSourceId()).isEqualTo(NOT_FOUND_ACCOUNT_ID);
    assertThat(result.getDestinationId()).isEqualTo(DST_ACCOUNT_ID);
    assertThat(result.getStatus()).isEqualTo(Transaction.TransactionStatus.FAILED);
    assertThat(result.getMessage()).containsIgnoringCase("The Source account is not found");
  }

  @Test
  public void shouldFailedWhenDstAccountNotFound() {
    // given
    Transaction transaction = new Transaction(SRC_ACCOUNT_ID, NOT_FOUND_ACCOUNT_ID, BigDecimal.ZERO);
    when(queue.poll()).thenReturn(transaction);
    when(accountService.get(eq(SRC_ACCOUNT_ID))).thenReturn(new Account());
    when(accountService.get(eq(NOT_FOUND_ACCOUNT_ID))).thenThrow(AccountNotFoundException.class);

    // when
    Transaction result = uut.processNext();

    //then
    verify(queue).poll();
    verify(accountService).get(eq(SRC_ACCOUNT_ID));
    verify(accountService).get(eq(NOT_FOUND_ACCOUNT_ID));

    assertThat(result.getAmount()).isEqualTo(BigDecimal.ZERO);
    assertThat(result.getSourceId()).isEqualTo(SRC_ACCOUNT_ID);
    assertThat(result.getDestinationId()).isEqualTo(NOT_FOUND_ACCOUNT_ID);
    assertThat(result.getStatus()).isEqualTo(Transaction.TransactionStatus.FAILED);
    assertThat(result.getMessage()).containsIgnoringCase("The Destination account is not found");
  }

  @Test
  public void shouldFailedWhenInsufficientFund() {
    // given
    Account src = new Account(SRC_ACCOUNT_ID, BigDecimal.ONE),
        dest = new Account(DST_ACCOUNT_ID, BigDecimal.ZERO);
    Transaction transaction = new Transaction(SRC_ACCOUNT_ID, DST_ACCOUNT_ID, BigDecimal.TEN);
    when(queue.poll()).thenReturn(transaction);
    when(accountService.get(eq(SRC_ACCOUNT_ID))).thenReturn(src);
    when(accountService.get(eq(DST_ACCOUNT_ID))).thenReturn(dest);

    // when
    Transaction result = uut.processNext();

    //then
    verify(queue).poll();
    verify(accountService).lockAccounts(eq(src), eq(dest));
    verify(accountService).get(eq(SRC_ACCOUNT_ID));
    verify(accountService).get(eq(DST_ACCOUNT_ID));
    verify(accountService).unlockAccounts(eq(src), eq(dest));

    assertThat(result.getAmount()).isEqualTo(BigDecimal.TEN);
    assertThat(result.getSourceId()).isEqualTo(SRC_ACCOUNT_ID);
    assertThat(result.getDestinationId()).isEqualTo(DST_ACCOUNT_ID);
    assertThat(result.getStatus()).isEqualTo(Transaction.TransactionStatus.FAILED);
    assertThat(result.getMessage()).containsIgnoringCase("Insufficient fund");
  }

  @Test
  public void shouldExecuteAndUpdateAccounts() {
    // given
    Account src = new Account(SRC_ACCOUNT_ID, BigDecimal.ONE),
        dest = new Account(DST_ACCOUNT_ID, BigDecimal.ZERO);
    Transaction transaction = new Transaction(SRC_ACCOUNT_ID, DST_ACCOUNT_ID, BigDecimal.ONE);
    when(queue.poll()).thenReturn(transaction);
    when(accountService.get(eq(SRC_ACCOUNT_ID))).thenReturn(src);
    when(accountService.get(eq(DST_ACCOUNT_ID))).thenReturn(dest);

    // when
    Transaction result = uut.processNext();

    //then
    verify(queue).poll();
    verify(accountService).lockAccounts(eq(src), eq(dest));
    verify(accountService).get(eq(SRC_ACCOUNT_ID));
    verify(accountService).get(eq(DST_ACCOUNT_ID));
    verify(accountService).unlockAccounts(eq(src), eq(dest));

    ArgumentCaptor<Account> accountsArgument = ArgumentCaptor.forClass(Account.class);
    verify(accountService, times(2)).update(accountsArgument.capture());

    assertThat(result.getAmount()).isEqualTo(BigDecimal.ONE);
    assertThat(result.getSourceId()).isEqualTo(SRC_ACCOUNT_ID);
    assertThat(result.getDestinationId()).isEqualTo(DST_ACCOUNT_ID);
    assertThat(result.getStatus()).isEqualTo(Transaction.TransactionStatus.EXECUTED);
    assertThat(result.getMessage()).isNull();

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
        bind(AccountService.class)
            .toInstance(mock(AccountService.class));
      }
    };
  }
}