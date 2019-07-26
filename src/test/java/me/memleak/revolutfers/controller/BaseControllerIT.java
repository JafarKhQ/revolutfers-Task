package me.memleak.revolutfers.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.ObjectMapper;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import me.memleak.revolutfers.ServerStartup;
import me.memleak.revolutfers.controller.model.ModelResponce;
import me.memleak.revolutfers.events.NewTransactionEvent;
import me.memleak.revolutfers.guicemodule.MyGuiceModule;
import me.memleak.revolutfers.service.AccountService;
import me.memleak.revolutfers.service.QueueExecutor;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import java.io.IOException;

import static org.mockito.Mockito.mock;

public class BaseControllerIT {

  private static final int port = 8000;
  private static final String url = "http://localhost:" + port + "/";

  static Injector injector;
  private static ServerStartup server;
  private static com.fasterxml.jackson.databind.ObjectMapper jacksonObjectMapper;

  @BeforeClass
  public static void beforeClass() throws Exception {
    injector = Guice.createInjector(new MockedGuiceModule());
    server = injector.getInstance(ServerStartup.class)
        .boot(port);
    jacksonObjectMapper = injector.getInstance(com.fasterxml.jackson.databind.ObjectMapper.class);

    setupUnirest();
  }

  @AfterClass
  public static void afterClass() throws Exception {
    server.shutdown();
  }

  <T> ModelResponce<T> get(String path, Class<T> clazz) throws UnirestException, IOException {
    HttpResponse<String> response = Unirest.get(url + path)
        .asString();

    return toObject(response.getBody(), clazz);
  }

  <T> ModelResponce<T> post(String path, Object body, Class<T> clazz) throws UnirestException, IOException {
    HttpResponse<String> response = Unirest.post(url + path)
        .body(body)
        .asString();

    return toObject(response.getBody(), clazz);
  }

  private <T> ModelResponce<T> toObject(String body, Class<T> clazz) throws IOException {
    JavaType t = jacksonObjectMapper.getTypeFactory().constructParametricType(ModelResponce.class, clazz);
    return jacksonObjectMapper.readValue(body, t);
  }

  private static void setupUnirest() {
    Unirest.setObjectMapper(new ObjectMapper() {

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
      bind(QueueExecutor.class)
          .toInstance(mock(QueueExecutor.class));
      bind(NewTransactionEvent.class)
          .toInstance(mock(NewTransactionEvent.class));
    }
  }
}
