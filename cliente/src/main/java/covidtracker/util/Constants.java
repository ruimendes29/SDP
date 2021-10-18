package covidtracker.util;

import org.zeromq.ZMQ;

@SuppressWarnings("checkstyle:hideutilityclassconstructor")
public final class Constants {
    public static ZMQ.Socket getSubSocket() {
        return subSocket;
    }

    public static String[] getDistritos() {
        return DISTRITOS;
    }

    public static void setSubSocket(final ZMQ.Socket subSocket) {
        Constants.subSocket = subSocket;
    }

    public static ZMQ.Socket getPrivateSocket() {
        return privateSocket;
    }

    public static void setPrivateSocket(final ZMQ.Socket privateSocket) {
        Constants.privateSocket = privateSocket;
    }

    public static String getUsername() {
        return username;
    }

    public static void setUsername(final String username) {
        Constants.username = username;
    }

    public static String getPassword() {
        return password;
    }

    public static void setPassword(final String password) {
        Constants.password = password;
    }

    public static boolean isLogged() {
        return logged;
    }

    public static void setLogged(final boolean logged) {
        Constants.logged = logged;
    }

    private static final String[] DISTRITOS =
            new String[] {
                "Viana do Castelo",
                "Braga",
                "Vila Real",
                "Braganca",
                "Porto",
                "Aveiro",
                "Viseu",
                "Guarda",
                "Coimbra",
                "Castelo Branco",
                "Leiria",
                "Santarem",
                "Lisboa",
                "Portalegre",
                "Evora",
                "Setubal",
                "Beja",
                "Faro",
                "Acores",
                "Madeira"
            };
    private static ZMQ.Socket subSocket;
    private static ZMQ.Socket privateSocket;
    private static String username;
    private static String password;
    private static boolean logged = false;
}
