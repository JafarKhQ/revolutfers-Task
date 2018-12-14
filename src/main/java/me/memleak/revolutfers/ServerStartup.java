package me.memleak.revolutfers;

import io.javalin.Javalin;
import me.memleak.revolutfers.controller.AccountController;
import me.memleak.revolutfers.controller.TransactionController;
import me.memleak.revolutfers.exception.AccountNotFoundException;
import org.eclipse.jetty.http.HttpStatus;

import javax.inject.Inject;
import javax.inject.Singleton;

import static io.javalin.apibuilder.ApiBuilder.*;

@Singleton
public class ServerStartup {
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

  void boot() {
    setupRoutes(app);
    setupExceptions(app);

    app.start();
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
        post(transactionController::doTransaction);
      });
    });
  }

  private void setupExceptions(Javalin app) {
    app.exception(AccountNotFoundException.class, (e, ctx) -> {
      ctx.status(HttpStatus.NOT_FOUND_404).result(e.getMessage());
    }).exception(Exception.class, (e, ctx) -> {
      ctx.status(HttpStatus.INTERNAL_SERVER_ERROR_500).result(e.getMessage());
    });
  }
}
