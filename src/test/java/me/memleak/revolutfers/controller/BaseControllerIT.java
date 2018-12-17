package me.memleak.revolutfers.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.ObjectMapper;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import me.memleak.revolutfers.ServerStartup;
import me.memleak.revolutfers.events.NewTransactionEvent;
import me.memleak.revolutfers.guicemodule.MyGuiceModule;
import me.memleak.revolutfers.service.AccountService;
import me.memleak.revolutfers.service.QueueExecutor;
import me.memleak.revolutfers.service.TransactionService;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import java.io.IOException;

import static org.mockito.Mockito.mock;

public class BaseControllerIT {

  private static final int port = 8000;
  private static final String url = "http://localhost:" + port + "/";

  static Injector injector;
  private static ServerStartup server;

  @BeforeClass
  public static void beforeClass() throws Exception {
    injector = Guice.createInjector(new MockedGuiceModule());
    server = injector.getInstance(ServerStartup.class)
        .boot(port);

    setupUnirest();
  }

  @AfterClass
  public static void afterClass() throws Exception {
    server.shutdown();
  }


  <T> T get(String path, Class<T> clazz) throws UnirestException {
    HttpResponse<T> response = Unirest.get(url + path)
        .asObject(clazz);

    return response.getBody();
  }

  <T> T post(String path, Object body, Class<T> clazz) throws UnirestException {
    HttpResponse<T> response = Unirest.post(url + path)
        .body(body)
        .asObject(clazz);

    return response.getBody();
  }

  private static void setupUnirest() {
    Unirest.setObjectMapper(new ObjectMapper() {
      private com.fasterxml.jackson.databind.ObjectMapper jacksonObjectMapper
          = new com.fasterxml.jackson.databind.ObjectMapper();

      public <T> T readValue(String value, Class<T> valueType) {
        try {
          return jacksonObjectMapper.readValue(value, valueType);
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      }

      public String writeValue(Object value) {
        try {
          return jacksonObjectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
          throw new RuntimeException(e);
        }
      }
    });
  }

  private static class MockedGuiceModule extends MyGuiceModule {
    @Override
    protected void configure() {
      bindApp();
      bind(AccountService.class)
          .toInstance(mock(AccountService.class));
      bind(TransactionService.class)
          .toInstance(mock(TransactionService.class));
      bind(QueueExecutor.class)
          .toInstance(mock(QueueExecutor.class));
      bind(NewTransactionEvent.class)
          .toInstance(mock(NewTransactionEvent.class));
    }
  }
}
