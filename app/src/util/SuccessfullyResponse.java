package util;

/**
 * SuccessfullyResponse es la clase que se utiliza para indicar, a modo
 * de mensaje, que una peticion HTTP fue satisfactoriamente realizada
 */
public class SuccessfullyResponse {

  /*
   * El mensaje contiene uno de los motivos por los cuales
   * se puede satisfacer una peticion HTTP, los cuales,
   * estan definidos en el enum ReasonSuccess
   */
  private String message;

  public SuccessfullyResponse(ReasonSuccess reasonSatisfaction) {
    message = reasonSatisfaction.getReason();
  }

  public String getMessage() {
    return message;
  }

}
