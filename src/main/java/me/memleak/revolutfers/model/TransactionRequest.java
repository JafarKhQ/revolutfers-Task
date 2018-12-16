package me.memleak.revolutfers.model;

import java.util.Objects;

public class TransactionRequest {
  private long sourceAccount;
  private long destinationAccount;
  private double amount;

  public long getSourceAccount() {
    return sourceAccount;
  }

  public void setSourceAccount(long sourceAccount) {
    this.sourceAccount = sourceAccount;
  }

  public long getDestinationAccount() {
    return destinationAccount;
  }

  public void setDestinationAccount(long destinationAccount) {
    this.destinationAccount = destinationAccount;
  }

  public double getAmount() {
    return amount;
  }

  public void setAmount(double amount) {
    this.amount = amount;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof TransactionRequest)) return false;
    TransactionRequest request = (TransactionRequest) o;
    return sourceAccount == request.sourceAccount &&
        destinationAccount == request.destinationAccount &&
        Double.compare(request.amount, amount) == 0;
  }

  @Override
  public int hashCode() {
    return Objects.hash(sourceAccount, destinationAccount, amount);
  }
}
