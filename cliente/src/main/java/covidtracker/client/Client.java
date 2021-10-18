package covidtracker.client;

import covidtracker.client.connection.ClientConnection;

public final class Client {
    private final int frontEndPort;

    public Client(final int frontEndPort) {
        this.frontEndPort = frontEndPort;
    }

    public void startUp() {
        new Thread(new ClientConnection(frontEndPort)).start();
    }
}
