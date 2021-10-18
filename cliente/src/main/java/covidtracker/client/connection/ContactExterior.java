package covidtracker.client.connection;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import covidtracker.util.Constants;
import javafx.scene.control.TextArea;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class ContactExterior implements Runnable {
    private static Logger log = LogManager.getLogger(ContactExterior.class);

    private final BufferedReader in;
    private final Set<Integer> subPorts;
    private boolean subscribed;
    private final TextArea terminal;

    public ContactExterior(final BufferedReader in, final TextArea terminal) {
        this.subscribed = false;
        this.subPorts = new HashSet<>();
        this.terminal = terminal;
        this.in = in;
    }

    @Override
    public void run() {
        try (ZContext context = new ZContext();
                ZMQ.Socket socket1 = context.createSocket(SocketType.SUB)) {
            Constants.setPrivateSocket(socket1);
            try (ZContext context1 = new ZContext();
                    ZMQ.Socket socket = context1.createSocket(SocketType.SUB)) {
                Constants.setSubSocket(socket);
                String line;
                while (true) {
                    line = in.readLine();
                    if (line != null) {
                        String[] args = line.split(" ");
                        if (args[0].equals("port")) {
                            int pport = Integer.parseInt(args[1]);
                            if (!subPorts.contains(pport)) {
                                socket.connect("tcp://localhost:" + pport);
                                subPorts.add(pport);
                                terminal.setText(
                                        terminal.getText()
                                                + "\nConnected successfully to port "
                                                + pport);
                            } else {
                                socket.disconnect("tcp://localhost:" + pport);
                                subPorts.remove(pport);
                                terminal.setText(
                                        terminal.getText()
                                                + "\nDisconnected successfully to port "
                                                + pport);
                            }
                            if (!subscribed) {
                                new Thread(new Subscriber(socket, terminal)).start();
                                subscribed = true;
                            }
                        } else if (args[0].equals("private_port")) {
                            System.out.println("found private port");
                            socket1.connect("tcp://localhost:" + Integer.parseInt(args[1]));
                            socket1.subscribe(Constants.getUsername().toLowerCase());
                            new Thread(new Subscriber(socket1, terminal)).start();
                        } else if (args[0].equals("Logged")) {
                            Constants.setLogged(true);
                            terminal.setText(terminal.getText() + "\n" + line);
                        } else {
                            terminal.setText(terminal.getText() + "\n" + line);
                        }
                    }
                }
            }
        } catch (IOException e) {
            log.fatal(e.getMessage(), e);
        }
    }
}
