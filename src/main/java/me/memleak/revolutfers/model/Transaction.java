package me.memleak.revolutfers.model;

import java.math.BigDecimal;
import java.util.Objects;

public class Transaction extends ModelId {

  public enum TransactionStatus {
    FAILED,
    PENDING,
    EXECUTED
  }

  private long sourceId;
  private long destinationId;
  private BigDecimal amount;
  private TransactionStatus status;
  private String message;

  public Transaction() {
    // make jackson happy :/
  }

  public Transaction(long sourceId, long destinationId, BigDecimal amount) {
    this(null, sourceId, destinationId, amount);
  }

  public Transaction(Long id, long sourceId, long destinationId, BigDecimal amount) {
    this.id = id;
    this.amount = amount;
    this.sourceId = sourceId;
    this.destinationId = destinationId;
    this.status = TransactionStatus.PENDING;
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

  public TransactionStatus getStatus() {
    return status;
  }

  public void setStatus(TransactionStatus status) {
    this.status = status;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Transaction)) return false;
    Transaction that = (Transaction) o;
    return id.equals(that.id) &&
        sourceId == that.sourceId &&
        destinationId == that.destinationId &&
        (amount.compareTo(that.amount) == 0) &&
        status == that.status &&
        Objects.equals(message, that.message);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, sourceId, destinationId, amount, status, message);
  }

  @Override
  public String toString() {
    return "Transaction{" +
        "id=" + id +
        '}';
  }
}
