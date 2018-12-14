package me.memleak.revolutfers.controller;

import io.javalin.Context;
import me.memleak.revolutfers.service.AccountService;
import org.eclipse.jetty.http.HttpStatus;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class AccountController {

  private final AccountService service;

  @Inject
  public AccountController(AccountService service) {
    this.service = service;
  }

  public void getAllAccounts(Context ctx) {
    ctx.json(service.getAll());
  }

  public void getAccount(Context ctx) {
    long id = ctx.validatedPathParam("id").asLong()
        .check(it -> it >= 0, "Id cant be negative.")
        .getOrThrow();

    ctx.json(service.get(id));
  }

  public void createAccount(Context ctx) {
    double amount = ctx.validatedBodyAsClass(Double.class)
        .check(it -> it >= 0, "Account balance cant be less than ZERO.")
        .getOrThrow();

    ctx.json(service.create(amount))
        .status(HttpStatus.CREATED_201);
  }
}
