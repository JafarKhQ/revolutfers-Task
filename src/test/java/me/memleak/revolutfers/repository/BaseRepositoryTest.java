package me.memleak.revolutfers.repository;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.junit.After;
import org.junit.Before;

public abstract class BaseRepositoryTest {

  Injector injector;

  abstract AbstractModule mockedGuiceModules();

  @Before
  public void setUp() throws Exception {
    injector = Guice.createInjector(mockedGuiceModules());
  }

  @After
  public void tearDown() throws Exception {
  }
}
