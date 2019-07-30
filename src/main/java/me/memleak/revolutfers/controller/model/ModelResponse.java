package me.memleak.revolutfers.controller.model;

public class ModelResponse<T> {
  private static final int SUCCESS = 0;
  private static final int JUST_ERROR = 1;

  public static <T> ModelResponse<T> ok(T body) {
    ModelResponse<T> r = new ModelResponse<>();
    r.setCode(SUCCESS);
    r.setBody(body);
    return r;
  }

  public static ModelResponse error(String msg) {
    ModelResponse<Object> r = new ModelResponse<>();
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

  @Override
  public String toString() {
    return "ModelResponse{" +
        "code=" + code +
        ", body=" + body +
        ", message='" + message + '\'' +
        '}';
  }
}
