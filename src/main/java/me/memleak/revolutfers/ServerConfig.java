package me.memleak.revolutfers;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.javalin.Javalin;
import io.javalin.http.BadRequestResponse;
import io.javalin.plugin.json.JavalinJackson;
import me.memleak.revolutfers.controller.AccountsController;
import me.memleak.revolutfers.controller.TransactionsController;
import me.memleak.revolutfers.exception.AccountNotFoundException;
import me.memleak.revolutfers.exception.InsufficientFundException;
import me.memleak.revolutfers.service.TransactionsService;
import org.eclipse.jetty.http.HttpStatus;

import javax.inject.Inject;
import javax.inject.Singleton;

import static io.javalin.apibuilder.ApiBuilder.*;
import static me.memleak.revolutfers.controller.model.ModelResponse.error;

@Singleton
public class ServerConfig {
  private static final int PORT = 7000;

  private final Javalin app;
  private final AccountsController accountsController;
  private final TransactionsController transactionsController;
  private final TransactionsService transactionsService;

  @Inject
  public ServerConfig(Javalin app,
                      AccountsController accountsController,
                      TransactionsController transactionsController,
                      TransactionsService transactionsService) {
    this.app = app;
    this.accountsController = accountsController;
    this.transactionsController = transactionsController;
    this.transactionsService = transactionsService;
  }

  public ServerConfig boot() {
    return boot(PORT);
  }

  public ServerConfig boot(int port) {
    jackson();
    setupRoutes(app);
    setupExceptions(app);

    app.start(port);
    return this;
  }

  public void shutdown() {
    transactionsService.stop();
    app.stop();
  }

  private void jackson() {
    JavalinJackson.getObjectMapper()
        .setSerializationInclusion(JsonInclude.Include.NON_NULL);
  }

  private void setupRoutes(Javalin app) {
    app.routes(() -> {
      path("accounts", () -> {
        get(accountsController::getAllAccounts);
        post(accountsController::createAccount);
        path(":id", () -> {
          get(accountsController::getAccount);
        });
      });

      path("transactions", () -> {
        post(transactionsController::createTransaction);
      });
    });
  }

  private void setupExceptions(Javalin app) {
    app.exception(AccountNotFoundException.class, (e, ctx) -> {
      ctx.status(HttpStatus.NOT_FOUND_404).json(error(e.getMessage()));
    }).exception(InsufficientFundException.class, (e, ctx) -> {
      ctx.status(HttpStatus.BAD_REQUEST_400).json(error(e.getMessage()));
    }).exception(BadRequestResponse.class, (e, ctx) -> {
      ctx.status(HttpStatus.BAD_REQUEST_400).json(error(e.getMessage()));
    }).exception(Exception.class, (e, ctx) -> {
      ctx.status(HttpStatus.INTERNAL_SERVER_ERROR_500).json(error(e.getMessage()));
    });
  }
}
