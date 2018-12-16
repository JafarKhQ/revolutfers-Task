package me.memleak.revolutfers.model;

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
}
