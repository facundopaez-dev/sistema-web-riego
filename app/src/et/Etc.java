package et;

public class Etc {

  /**
   * Retorna la evapotranspiracion del cultivo bajo condiciones
   * estandar (ETc), la cual, es necesaria para determinar la
   * necesidad de agua de riego de un cultivo.
   * 
   * La formula de la ETc es ETc = ETo (evapotranspiracion del
   * cultivo de referencia) * kc (coeficiente de cultivo)
   * 
   * La formula de la evapotranspiracion del cultivo de referencia
   * (ETo) se encuentra en la pagina 25 del libro "Evapotranspiracion
   * del cultivo" de la FAO.
   * 
   * @param eto [mm/dia]
   * @param kc  [admimensional]
   * @return punto flotante que representa la evapotranspiracion
   *         del cultivo bajo condiciones estandar [mm/dia]
   */
  public static double calculateEtc(double eto, double kc) {
    return eto * kc;
  }

}
