package me.memleak.revolutfers.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.ObjectMapper;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import me.memleak.revolutfers.ServerConfig;
import me.memleak.revolutfers.controller.model.ModelResponse;
import me.memleak.revolutfers.events.TransactionEvent;
import me.memleak.revolutfers.guicemodule.GuiceConfigurationModule;
import me.memleak.revolutfers.service.AccountsService;
import me.memleak.revolutfers.service.TransactionsService;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import java.io.IOException;

import static org.mockito.Mockito.mock;

public class BaseControllerTest {

  private static final int port = 8000;
  private static final String url = "http://localhost:" + port + "/";

  static Injector injector;
  private static ServerConfig server;
  private static com.fasterxml.jackson.databind.ObjectMapper jacksonObjectMapper;

  @BeforeClass
  public static void beforeClass() throws Exception {
    injector = Guice.createInjector(new MockedGuiceModule());
    server = injector.getInstance(ServerConfig.class)
        .boot(port);
    jacksonObjectMapper = injector.getInstance(com.fasterxml.jackson.databind.ObjectMapper.class);

    setupUnirest();
  }

  @AfterClass
  public static void afterClass() throws Exception {
    server.shutdown();
  }

  <T> ModelResponse<T> get(String path, Class<T> clazz) throws UnirestException, IOException {
    HttpResponse<String> response = Unirest.get(url + path)
        .asString();

    return toObject(response.getBody(), clazz);
  }

  <T> ModelResponse<T> post(String path, Object body, Class<T> clazz) throws UnirestException, IOException {
    HttpResponse<String> response = Unirest.post(url + path)
        .body(body)
        .asString();

    return toObject(response.getBody(), clazz);
  }

  private <T> ModelResponse<T> toObject(String body, Class<T> clazz) throws IOException {
    JavaType t = jacksonObjectMapper.getTypeFactory().constructParametricType(ModelResponse.class, clazz);
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

  private static class MockedGuiceModule extends GuiceConfigurationModule {
    @Override
    protected void configure() {
      bindApp();
      bind(AccountsService.class)
          .toInstance(mock(AccountsService.class));
      bind(TransactionsService.class)
          .toInstance(mock(TransactionsService.class));
      bind(TransactionEvent.class)
          .toInstance(mock(TransactionEvent.class));
    }
  }
}
