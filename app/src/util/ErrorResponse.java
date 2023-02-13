package util;

/**
 * ErrorResponse es la clase que se utiliza para indicar, a modo
 * de mensaje, el motivo por el cual no se puede satisfacer una
 * peticion HTTP
 */
public class ErrorResponse {

  /*
   * El mensaje contiene uno de los motivos por los cuales
   * no se puede satisfacer una peticion HTTP, los cuales,
   * estan definidos en el enum ReasonError
   */
  private String message;

  public ErrorResponse(ReasonError reasonError) {
    message = reasonError.getReason();
  }

  public String getMessage() {
    return message;
  }

}
