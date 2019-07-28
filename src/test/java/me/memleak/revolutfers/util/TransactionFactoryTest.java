package me.memleak.revolutfers.util;

import me.memleak.revolutfers.controller.model.TransactionRequest;
import me.memleak.revolutfers.model.Transaction;
import org.junit.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;


public class TransactionFactoryTest {

  @Test
  public void testCreatingTransactionFromRequest() {
    TransactionRequest request = new TransactionRequest();
    request.setSourceAccount(1);
    request.setDestinationAccount(2);
    request.setAmount(1.33);

    Transaction t = TransactionFactory.from(request);

    assertThat(t.getSourceId()).isEqualTo(1);
    assertThat(t.getDestinationId()).isEqualTo(2);
    assertThat(t.getAmount()).isEqualTo(BigDecimal.valueOf(1.33));
    assertThat(t.getMessage()).isNull();
    assertThat(t.getStatus()).isEqualTo(Transaction.TransactionStatus.PENDING);
  }
}
