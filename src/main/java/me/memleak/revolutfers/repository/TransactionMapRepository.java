package me.memleak.revolutfers.repository;

import me.memleak.revolutfers.model.Transaction;

import javax.inject.Singleton;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Singleton
public class TransactionMapRepository extends BaseMapRepository<Transaction> {

  public List<Transaction> findUnprocessed() {
    return findAll().stream()
        .filter(t -> Transaction.TransactionStatus.PENDING.equals(t.getStatus()))
        //.limit()
        .collect(toList());
  }
}
