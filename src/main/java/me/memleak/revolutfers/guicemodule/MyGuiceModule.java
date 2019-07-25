package me.memleak.revolutfers.guicemodule;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
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
    bind(NewTransactionEvent.class).to(QueueExecutor.class);
    bind(new TypeLiteral<Queue<Transaction>>(){}).toInstance(new ConcurrentLinkedQueue<>());
  }

  protected void bindApp() {
    bind(Javalin.class).toInstance(Javalin.create());
  }
}
