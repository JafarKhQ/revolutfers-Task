package me.memleak.revolutfers.exception;

import java.text.MessageFormat;

class NotSoUglyException extends RuntimeException {

  NotSoUglyException(String message, Object... params) {
    super(MessageFormat.format(message, params));
  }
}
