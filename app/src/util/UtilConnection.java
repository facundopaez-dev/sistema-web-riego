package util;

import java.net.InetAddress;

public class UtilConnection {

    /**
     * @return true si el servicio meteorologico utilizado
     * por la aplicacion es alcanzable, en caso contrario
     * false
     */
    public static boolean weatherServiceIsReachable() {

        try {
            InetAddress address = InetAddress.getByName("www.visualcrossing.com");
            address.isReachable(2000);
            return true;
        } catch (Exception e) {
            return false;
        }

    }

}