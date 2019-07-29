package me.memleak.revolutfers;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.javalin.Javalin;
import io.javalin.http.BadRequestResponse;
import io.javalin.plugin.json.JavalinJackson;
import me.memleak.revolutfers.controller.AccountController;
import me.memleak.revolutfers.controller.TransactionController;
import me.memleak.revolutfers.exception.AccountNotFoundException;
import me.memleak.revolutfers.exception.InsufficientFundException;
import me.memleak.revolutfers.service.QueueExecutor;
import org.eclipse.jetty.http.HttpStatus;

import javax.inject.Inject;
import javax.inject.Singleton;

import static io.javalin.apibuilder.ApiBuilder.*;
import static me.memleak.revolutfers.controller.model.ModelResponse.error;

@Singleton
public class ServerStartup {
  private static final int PORT = 7000;

  private final Javalin app;
  private final AccountController accountController;
  private final TransactionController transactionController;
  private final QueueExecutor queueExecutor;

  @Inject
  public ServerStartup(Javalin app,
                       AccountController accountController,
                       TransactionController transactionController,
                       QueueExecutor queueExecutor) {
    this.app = app;
    this.accountController = accountController;
    this.transactionController = transactionController;
    this.queueExecutor = queueExecutor;
  }

  public ServerStartup boot() {
    return boot(PORT);
  }

  public ServerStartup boot(int port) {
    jackson();
    setupRoutes(app);
    setupExceptions(app);

    app.start(port);
    return this;
  }

  public void shutdown() {
    queueExecutor.stop();
    app.stop();
  }

  private void jackson() {
    JavalinJackson.getObjectMapper()
        .setSerializationInclusion(JsonInclude.Include.NON_NULL);
  }

  private void setupRoutes(Javalin app) {
    app.routes(() -> {
      path("accounts", () -> {
        get(accountController::getAllAccounts);
        post(accountController::createAccount);
        path(":id", () -> {
          get(accountController::getAccount);
        });
      });

      path("transactions", () -> {
        post(transactionController::createTransaction);
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
