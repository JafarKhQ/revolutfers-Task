package me.memleak.revolutfers.controller.model;

public class ModelResponce<T> {
  private static final int SUCCESS = 0;
  private static final int JUST_ERROR = 1;

  public static <T> ModelResponce<T> ok(T body) {
    ModelResponce<T> r = new ModelResponce<>();
    r.setCode(SUCCESS);
    r.setBody(body);
    return r;
  }

  public static ModelResponce error(String msg) {
    ModelResponce<Object> r = new ModelResponce<>();
    r.setCode(JUST_ERROR);
    r.setMessage(msg);
    return r;
  }

  private int code;
  private T body;
  private String message;

  public int getCode() {
    return code;
  }

  public void setCode(int code) {
    this.code = code;
  }

  public T getBody() {
    return body;
  }

  public void setBody(T body) {
    this.body = body;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }
}
