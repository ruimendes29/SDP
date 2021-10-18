package covidtracker.client.connection;

import javafx.scene.control.TextArea;
import org.zeromq.ZMQ;

public final class Subscriber implements Runnable {
    private final ZMQ.Socket subSocket;
    private TextArea terminal;

    public Subscriber(final ZMQ.Socket subSocket, final TextArea terminal) {
        this.subSocket = subSocket;
        this.terminal = terminal;
    }

    @Override
    public void run() {
        while (true) {
            byte[] msg = subSocket.recv();
            String[] args = new String(msg).split(":");
            terminal.setText(terminal.getText() + "\n" + args[1]);
        }
    }
}
