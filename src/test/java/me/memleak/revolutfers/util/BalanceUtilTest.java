package me.memleak.revolutfers.util;

import org.junit.Test;

import java.math.BigDecimal;

import static me.memleak.revolutfers.util.BalanceUtil.toBankingBalance;
import static org.assertj.core.api.Assertions.assertThat;

public class BalanceUtilTest {

  @Test
  public void testToBankingBalance() {
    BigDecimal result = toBankingBalance(1.0000);
    assertThat(result).isEqualByComparingTo(BigDecimal.ONE);

    result = toBankingBalance(0.69);
    assertThat(result).isEqualByComparingTo(BigDecimal.valueOf(0.69));

    result = toBankingBalance(1.688);
    assertThat(result).isEqualByComparingTo(BigDecimal.valueOf(1.69));

    result = toBankingBalance(2.683);
    assertThat(result).isEqualByComparingTo(BigDecimal.valueOf(2.69));
  }
}