package me.memleak.revolutfers.controller;

import io.javalin.http.Context;
import me.memleak.revolutfers.controller.model.AccountRequest;
import me.memleak.revolutfers.service.AccountsService;
import org.eclipse.jetty.http.HttpStatus;

import javax.inject.Inject;
import javax.inject.Singleton;

import static me.memleak.revolutfers.controller.model.ModelResponse.ok;

@Singleton
public class AccountsController {

  private final AccountsService service;

  @Inject
  public AccountsController(AccountsService service) {
    this.service = service;
  }

  public void getAllAccounts(Context ctx) {
    ctx.json(ok(service.getAll()))
        .status(HttpStatus.OK_200);
  }

  public void getAccount(Context ctx) {
    long id = ctx.pathParam("id", Long.class)
        .check(it -> it >= 0, "Id cant be negative.")
        .get();

    ctx.json(ok(service.get(id)))
        .status(HttpStatus.OK_200);
  }

  public void createAccount(Context ctx) {
    AccountRequest request = ctx.bodyValidator(AccountRequest.class)
        .check(it -> it.getBalance() >= 0, "Account balance cant be less than ZERO.")
        .get();

    ctx.json(ok(service.create(request)))
        .status(HttpStatus.CREATED_201);
  }
}
