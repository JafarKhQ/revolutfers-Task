package me.memleak.revolutfers.service;

import me.memleak.revolutfers.exception.AccountNotFoundException;
import me.memleak.revolutfers.model.Account;
import me.memleak.revolutfers.model.ModelId;
import me.memleak.revolutfers.model.Transaction;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.text.MessageFormat;
import java.util.concurrent.locks.Lock;
import java.util.stream.Stream;

import static java.util.Comparator.comparing;

@Singleton
public class TransactionProcessor {

  private final AccountService accountService;
  private final TransactionService transactionService;

  @Inject
  public TransactionProcessor(AccountService accountService, TransactionService transactionService) {
    this.accountService = accountService;
    this.transactionService = transactionService;
  }

  public void process(Transaction transaction) {
    Account src = null, dest = null;
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
