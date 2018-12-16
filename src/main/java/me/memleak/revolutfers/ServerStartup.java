package me.memleak.revolutfers;

import io.javalin.Javalin;
import me.memleak.revolutfers.controller.AccountController;
import me.memleak.revolutfers.controller.TransactionController;
import me.memleak.revolutfers.exception.AccountNotFoundException;
import me.memleak.revolutfers.exception.TransactionNotFoundException;
import org.eclipse.jetty.http.HttpStatus;

import javax.inject.Inject;
import javax.inject.Singleton;

import static io.javalin.apibuilder.ApiBuilder.*;

@Singleton
public class ServerStartup {
  private static final int PORT = 7000;

  private final Javalin app;
  private final AccountController accountController;
  private final TransactionController transactionController;

  @Inject
  public ServerStartup(Javalin app,
                       AccountController accountController,
                       TransactionController transactionController) {
    this.app = app;
    this.accountController = accountController;
    this.transactionController = transactionController;
  }

  public ServerStartup boot() {
    return boot(PORT);
  }

  public ServerStartup boot(int port) {
    setupRoutes(app);
    setupExceptions(app);
    app.start(port);

    return this;
  }

  public void shutdown() {
    app.stop();
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

      path("transaction", () -> {
        post(transactionController::createTransaction);
      });
    });
  }

  private void setupExceptions(Javalin app) {
    app.exception(AccountNotFoundException.class, (e, ctx) -> {
      ctx.status(HttpStatus.NOT_FOUND_404).result(e.getMessage());
    }).exception(TransactionNotFoundException.class, (e, ctx) -> {
      ctx.status(HttpStatus.NOT_FOUND_404).result(e.getMessage());
    }).exception(Exception.class, (e, ctx) -> {
      ctx.status(HttpStatus.INTERNAL_SERVER_ERROR_500).result(e.getMessage());
    });
  }
}
