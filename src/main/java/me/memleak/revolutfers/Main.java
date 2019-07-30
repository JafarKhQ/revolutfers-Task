package me.memleak.revolutfers;

import com.google.inject.Guice;
import com.google.inject.Injector;
import me.memleak.revolutfers.guicemodule.GuiceConfigurationModule;

public class Main {

  public static void main(String[] args) {
    Injector injector = Guice.createInjector(new GuiceConfigurationModule());
    injector.getInstance(ServerConfig.class).boot();
  }
}
