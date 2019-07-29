package me.memleak.revolutfers.guicemodule;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;
import io.javalin.Javalin;
import me.memleak.revolutfers.events.TransactionEvent;
import me.memleak.revolutfers.model.Transaction;
import me.memleak.revolutfers.service.TransactionsService;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class GuiceConfigurationModule extends AbstractModule {

  @Override
  protected void configure() {
    bindApp();
    bindProps();
    bind(TransactionEvent.class).to(TransactionsService.class);
    bind(new TypeLiteral<Queue<Transaction>>(){}).toInstance(new ConcurrentLinkedQueue<>());
  }

  private void bindProps() {
    bind(Integer.class)
        .annotatedWith(Names.named("transactions.thread.size"))
        .toInstance(4);
  }

  protected void bindApp() {
    bind(Javalin.class).toInstance(Javalin.create());
  }
}
