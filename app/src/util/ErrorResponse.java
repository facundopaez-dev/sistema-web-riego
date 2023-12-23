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

  /*
   * La respuesta de error contiene la fuente en la que no
   * se pudo satisfacer una peticion HTTP
   */
  private String sourceUnsatisfiedResponse;

  public ErrorResponse(ReasonError reasonError) {
    message = reasonError.getReason();
  }

  public ErrorResponse(ReasonError reasonError, SourceUnsatisfiedResponse sourceUnsatisfiedResponse) {
    message = reasonError.getReason();
    this.sourceUnsatisfiedResponse = sourceUnsatisfiedResponse.getOrigin();
  }

  public ErrorResponse(String message, SourceUnsatisfiedResponse sourceUnsatisfiedResponse) {
    this.message = message;
    this.sourceUnsatisfiedResponse = sourceUnsatisfiedResponse.getOrigin();
  }

  public String getMessage() {
    return message;
  }

  public String getSourceUnsatisfiedResponse() {
    return sourceUnsatisfiedResponse;
  }

}
