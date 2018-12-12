package me.memleak.revolutfers;

import io.javalin.Javalin;
import me.memleak.revolutfers.controller.AccountController;

import static io.javalin.apibuilder.ApiBuilder.*;

public class Main {

  public static void main(String[] args) {
    AccountController controller = new AccountController();
    Javalin app = Javalin.create()
        .start(7000);

    app.get("/", ctx -> ctx.result("Hello World"));

    app.routes(() -> {
      path("accounts", () -> {
        get(controller::getAllAccounts);
        post(controller::createAccount);
        path(":id", () -> {
          get(controller::getAccount);
        });
      });

      path("transaction", () -> {
        post(null);
      });
    });

  }
}
