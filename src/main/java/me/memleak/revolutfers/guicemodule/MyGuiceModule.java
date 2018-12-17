package me.memleak.revolutfers.guicemodule;

import com.google.inject.AbstractModule;
import io.javalin.Javalin;
import me.memleak.revolutfers.events.NewTransactionEvent;
import me.memleak.revolutfers.service.QueueExecutor;

public class MyGuiceModule extends AbstractModule {

  @Override
  protected void configure() {
    bindApp();
    bind(NewTransactionEvent.class).to(QueueExecutor.class);
  }

  protected void bindApp() {
    bind(Javalin.class).toInstance(Javalin.create());
  }
}
