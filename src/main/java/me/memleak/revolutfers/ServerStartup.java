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
  private final AccountController accountController;
  private final TransactionController transactionController;

  @Inject
  public ServerStartup(AccountController accountController, TransactionController transactionController) {
    this.accountController = accountController;
    this.transactionController = transactionController;
  }

  public void boot() {
    Javalin app = Javalin.create()
        .start(7000);

    app.get("/", ctx -> ctx.result("Hello World"));

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

    app.exception(AccountNotFoundException.class, (e, ctx) -> {
      ctx.status(HttpStatus.NOT_FOUND_404).result(e.getMessage());
    }).exception(Exception.class, (e, ctx) -> {
      ctx.status(HttpStatus.INTERNAL_SERVER_ERROR_500).result(e.getMessage());
    });
  }
}
