package me.memleak.revolutfers.events;

import me.memleak.revolutfers.model.Transaction;

import java.util.concurrent.Future;

public interface NewTransactionEvent {

  Future<Transaction> onNewTransaction(Transaction transaction);
}
