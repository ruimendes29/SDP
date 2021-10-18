package covidtracker.client.connection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import covidtracker.view.Color;
import covidtracker.view.Terminal;

public final class ClientConnection implements Runnable {
    private static Logger log = LogManager.getLogger(ClientConnection.class);

    private int frontEndPort;
    private ZMQ.Socket subSocket;
    private Set<String> subscribedDist;

    public ClientConnection(final int frontEndPort) {
        this.frontEndPort = frontEndPort;
        subscribedDist = new HashSet<>();
    }

    private void subscribe(final String theme, final String district) {
        if (subscribedDist.contains(district)) {
            // args[1] should name the district
            subSocket.subscribe(theme + " " + district);
            Terminal.show("Subscribed successfully!", Color.ANSI_YELLOW);
        } else Terminal.show("Subscribe to " + district + " first!", Color.ANSI_BLUE);
    }

    @Override
    public void run() {
        try (ZContext context = new ZContext();
                ZMQ.Socket socket = context.createSocket(SocketType.SUB)) {
            Socket toFrontEnd = new Socket("localhost", frontEndPort);
            BufferedReader in =
                    new BufferedReader(new InputStreamReader(toFrontEnd.getInputStream()));
            PrintWriter out = new PrintWriter(toFrontEnd.getOutputStream());
            BufferedReader inClient = new BufferedReader(new InputStreamReader(System.in));
            this.subSocket = socket;
            String line;
            new Thread(new ContactExterior(in, null)).start();
            while ((line = inClient.readLine()) != null) {
                String[] args = line.split(" ");
                switch (args[0]) {
                    case "sub_empty":
                        subscribe("free", args[1]);
                        break;
                    case "sub_infected":
                        subscribe("infected", args[1]);
                        break;
                    case "sub_more":
                        subscribe("aumento", args[1]);
                        break;
                    case "sub_less":
                        subscribe("diminuicao", args[1]);
                        break;
                    case "get_nots":
                        subscribedDist.add(args[1]);
                    default:
                        out.println(line);
                        out.flush();
                }
            }
        } catch (IOException e) {
            log.fatal(e.getMessage(), e);
        }
    }
}
