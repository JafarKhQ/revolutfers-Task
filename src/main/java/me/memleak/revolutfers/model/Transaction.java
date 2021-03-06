package me.memleak.revolutfers.model;

import java.math.BigDecimal;
import java.util.Objects;

public class Transaction {

  private long sourceId;
  private long destinationId;
  private BigDecimal amount;
  private String status;

  public Transaction() {
    this(0, 0, BigDecimal.ZERO);
  }

  public Transaction(long sourceId, long destinationId, BigDecimal amount) {
    this.amount = amount;
    this.sourceId = sourceId;
    this.destinationId = destinationId;
  }

  public long getSourceId() {
    return sourceId;
  }

  public void setSourceId(long sourceId) {
    this.sourceId = sourceId;
  }

  public long getDestinationId() {
    return destinationId;
  }

  public void setDestinationId(long destinationId) {
    this.destinationId = destinationId;
  }

  public BigDecimal getAmount() {
    return amount;
  }

  public void setAmount(BigDecimal amount) {
    this.amount = amount;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Transaction)) return false;
    Transaction that = (Transaction) o;
    return sourceId == that.sourceId &&
        destinationId == that.destinationId &&
        (amount.compareTo(that.amount) == 0) &&
        Objects.equals(status, that.status);
  }

  @Override
  public int hashCode() {
    return Objects.hash(sourceId, destinationId, amount, status);
  }

  @Override
  public String toString() {
    return "Transaction{" +
        "sourceId=" + sourceId +
        ", destinationId=" + destinationId +
        ", amount=" + amount +
        ", status=" + status +
        '}';
  }
}
