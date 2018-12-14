package me.memleak.revolutfers.guicemodule;

import com.google.inject.AbstractModule;
import io.javalin.Javalin;

public class MyGuiceModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(Javalin.class).toInstance(Javalin.create());
  }
}
