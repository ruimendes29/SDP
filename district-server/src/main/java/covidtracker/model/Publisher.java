package covidtracker.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class Publisher implements Runnable {
    private static Logger log = LogManager.getLogger(Publisher.class);

    private int port;
    private ZMQ.Socket pubSocket;
    private BufferedReader in;

    public Publisher(final int portExt, final int portInt) {
        try {
            this.port = portExt;
            Socket s = new Socket("localhost", portInt);
            this.in = new BufferedReader(new InputStreamReader(s.getInputStream()));
        } catch (IOException e) {
            log.fatal(e.getMessage(), e);
        }
    }

    @Override
    public void run() {
        try (ZContext context = new ZContext();
                ZMQ.Socket socket = context.createSocket(SocketType.PUB)) {
            this.pubSocket = socket;
            this.pubSocket.bind("tcp://*:" + this.port);
            String line;
            while ((line = in.readLine()) != null) {
                log.trace("Will send " + line);
                this.pubSocket.send(line.getBytes());
            }
        } catch (IOException e) {
            log.fatal(e.getMessage(), e);
        }
    }
}
