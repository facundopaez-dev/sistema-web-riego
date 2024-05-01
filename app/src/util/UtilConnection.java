package util;

import java.net.URL;
import java.net.URLConnection;

public class UtilConnection {

    /**
     * @return true si hay conexion a Internet, false en
     * caso contrario
     */
    public static boolean checkInternetConnection() {

        try {
            URL url = new URL("https://weather.visualcrossing.com");
            URLConnection connection = url.openConnection();
            connection.connect();
            return true;
        } catch (Exception e) {
            return false;
        }

    }

}