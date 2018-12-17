package me.memleak.revolutfers.events;

import me.memleak.revolutfers.model.Transaction;

public interface NewTransactionEvent {

  void onNewTransaction(Transaction transaction);
}
