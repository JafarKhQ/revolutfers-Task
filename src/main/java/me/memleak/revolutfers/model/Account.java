package me.memleak.revolutfers.model;

import java.math.BigDecimal;

public class Account {
  private final Long id;
  private BigDecimal balance;

  public Account(Long id) {
    this(id, BigDecimal.ZERO);
  }

  public Account(Long id, BigDecimal balance) {
    this.id = id;
    this.balance = balance;
  }

  public Long getId() {
    return id;
  }

  public BigDecimal getBalance() {
    return balance;
  }

  public void setBalance(BigDecimal balance) {
    this.balance = balance;
  }
}
