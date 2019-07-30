package me.memleak.revolutfers.exception;

public class ItemNotFoundException extends NotSoUglyException {

  public ItemNotFoundException(String message, Object... params) {
    super(message, params);
  }
}
