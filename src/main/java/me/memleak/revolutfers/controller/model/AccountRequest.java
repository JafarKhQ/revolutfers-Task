package me.memleak.revolutfers.controller.model;

import java.util.Objects;

public class AccountRequest {

  private double balance;

  public double getBalance() {
    return balance;
  }

  public void setBalance(double balance) {
    this.balance = balance;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    AccountRequest request = (AccountRequest) o;
    return Double.compare(request.balance, balance) == 0;
  }

  @Override
  public int hashCode() {
    return Objects.hash(balance);
  }
}
