package me.memleak.revolutfers.service;

import me.memleak.revolutfers.exception.TransactionNotFoundException;
import me.memleak.revolutfers.model.ModelId;
import me.memleak.revolutfers.model.Transaction;
import me.memleak.revolutfers.controller.model.TransactionRequest;
import me.memleak.revolutfers.repository.TransactionMapRepository;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;
import static me.memleak.revolutfers.util.BalanceUtil.toBankingBalance;

@Singleton
public class TransactionService {

  private final TransactionMapRepository repository;

  @Inject
  public TransactionService(TransactionMapRepository repository) {
    this.repository = repository;
  }

  public List<Transaction> getAll() {
    return repository.findAll();
  }

  public List<Transaction> getUnprocessed() {
    return repository.findUnprocessed().stream()
        .sorted(comparing(ModelId::getId))
        .collect(toList());
  }

  public Transaction update(Transaction transaction) {
    return repository.update(transaction);
  }

  public Transaction get(long id) {
    return repository.find(id)
        .orElseThrow(() -> new TransactionNotFoundException("Cant find Transaction with id: {0}", id));
  }

  public Transaction create(TransactionRequest request) {
    Transaction transaction = new Transaction(request.getSourceAccount(), request.getDestinationAccount(),
        toBankingBalance.apply(request.getAmount()));

    return repository.create(transaction);
  }
}
