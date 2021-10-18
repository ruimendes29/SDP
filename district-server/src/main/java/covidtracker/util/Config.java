package covidtracker.util;

import java.util.Optional;

public final class Config {
    public static final String DIRECTORY_HOST =
            Optional.ofNullable(System.getenv("DIRECTORY_SERVER_HOSTNAME")).orElse("127.0.0.1");
    public static final int DIRECTORY_PORT =
            Integer.parseInt(
                    Optional.ofNullable(System.getenv("DIRECTORY_SERVER_PORT")).orElse("9090"));

    private Config() {}
}
