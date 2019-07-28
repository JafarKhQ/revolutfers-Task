package me.memleak.revolutfers.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.function.DoubleFunction;

public final class BalanceUtil {

  public static BigDecimal toBankingBalance(double d) {
    return BigDecimal.valueOf(d).setScale(2, RoundingMode.CEILING);
  }
}
