package me.memleak.revolutfers.exception;

public class TransactionNotFoundException extends NotSoUglyException {

  public TransactionNotFoundException(String message, Object... params) {
    super(message, params);
  }
}
