package me.memleak.revolutfers.exception;

import static java.text.MessageFormat.format;

class NotSoUglyException extends RuntimeException {

  NotSoUglyException(String message, Object... params) {
    super(format(message, params));
  }
}
