package util;

/**
 * SourceUnsatisfiedResponse es el enum que contiene el origen
 * o la fuente (como constante de enumeracion) en la que NO se
 * puede satisfacer una peticion HTTP.
 * 
 * Por ejemplo, si una solicitud HTTP relacionada a un cultivo
 * no se puede satisfacer por algun motivo, el origen o la fuente
 * en la que NO se puede satisfacer dicha peticion es el cultivo.
 */
public enum SourceUnsatisfiedResponse {
  CROP("ORIGIN_CROP"),
  SOIL("ORIGIN_SOIL"),
  REGION("ORIGIN_REGION"),
  TYPE_CROP("ORIGIN_TYPE_CROP"),
  PARCEL("ORIGIN_PARCEL"),
  WATER_NEED_CROP("ORIGIN_NEED_WATER_CROP"),
  DEAD_CROP_WATER_NEED("ORIGIN_DEAD_CROP_WATER_NEED"),
  NON_EXISTENT_DATA_FOR_STATISTICAL_REPORT("ORIGIN_NON_EXISTENT_DATA_FOR_STATISTICAL_REPORT");

  private final String origin;

  private SourceUnsatisfiedResponse(String origin) {
    this.origin = origin;
  }

  public String getOrigin() {
    return origin;
  }

}