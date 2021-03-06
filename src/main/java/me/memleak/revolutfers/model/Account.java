package me.memleak.revolutfers.model;

import java.math.BigDecimal;
import java.util.Objects;

public class Account extends ModelId {

  private BigDecimal balance;

  public Account() {
    // make jackson happy :/
  }

  public Account(Long id) {
    this(id, BigDecimal.ZERO);
  }

  public Account(BigDecimal balance) {
    this(null, balance);
  }

  public Account(Long id, BigDecimal balance) {
    this.id = id;
    this.balance = balance;
  }

  public BigDecimal getBalance() {
    return balance;
  }

  public void setBalance(BigDecimal balance) {
    this.balance = balance;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Account account = (Account) o;
    return id.equals(account.id) &&
        (balance.compareTo(account.balance) == 0);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, balance);
  }
}
