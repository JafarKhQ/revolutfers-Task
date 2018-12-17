package me.memleak.revolutfers.exception;

public class AccountNotFoundException extends NotSoUglyException {

  public AccountNotFoundException(String message, Object... params) {
    super(message, params);
  }
}
