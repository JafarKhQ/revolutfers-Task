package me.memleak.revolutfers.util;

import me.memleak.revolutfers.controller.model.TransactionRequest;
import me.memleak.revolutfers.model.Transaction;

public final class TransactionFactory {

  public static Transaction from(TransactionRequest transactionRequest) {
    Transaction transaction = new Transaction();
    transaction.setStatus(Transaction.TransactionStatus.PENDING);
    transaction.setSourceId(transactionRequest.getSourceAccount());
    transaction.setDestinationId(transactionRequest.getDestinationAccount());
    transaction.setAmount(BalanceUtil.toBankingBalance(transactionRequest.getAmount()));
    return transaction;
  }

}
