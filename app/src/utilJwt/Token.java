package utilJwt;

/*
 * Token es la clase que se utiliza para almacenar y retornar
 * un JWT ante un satisfactorio inicio de sesion de un usuario
 */
public class Token {

  private String jwt;

  public Token(String jwt) {
    this.jwt = jwt;
  }

  public String getJwt() {
    return jwt;
  }

}
