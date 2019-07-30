package me.memleak.revolutfers.exception;

public class InsufficientFundException extends NotSoUglyException {

  public InsufficientFundException(String message, Object... params) {
    super(message, params);
  }
}
