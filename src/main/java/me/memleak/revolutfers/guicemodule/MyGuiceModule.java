package me.memleak.revolutfers.guicemodule;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;
import io.javalin.Javalin;
import me.memleak.revolutfers.events.NewTransactionEvent;
import me.memleak.revolutfers.model.Transaction;
import me.memleak.revolutfers.service.QueueExecutor;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class MyGuiceModule extends AbstractModule {

  @Override
  protected void configure() {
    bindApp();
    bindProps();
    bind(NewTransactionEvent.class).to(QueueExecutor.class);
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
