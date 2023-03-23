package util;

/**
 * PersonalizedResponse es la clase que se utiliza para
 * que la aplicacion del lado servidor retorne respuestas
 * que no se pueden enviar con constantes de enumeracion
 */
public class PersonalizedResponse {

    private String message;

    public PersonalizedResponse(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

}