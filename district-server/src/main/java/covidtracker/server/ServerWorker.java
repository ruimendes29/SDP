package covidtracker.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import covidtracker.exceptions.DuplicateUserException;
import covidtracker.exceptions.NonExistentUserException;

import covidtracker.client.HttpClient;
import covidtracker.model.District;
import covidtracker.model.Location;
import covidtracker.util.Config;

public final class ServerWorker implements Runnable {
    private static Logger log = LogManager.getLogger(ServerWorker.class);

    private Socket socket;
    private District district;
    private HttpClient directory;

    public ServerWorker(final Socket socket, final District district) {
        this.socket = socket;
        this.district = district;
        this.directory =
                new HttpClient.Builder()
                        .hostname(Config.DIRECTORY_HOST)
                        .port(Config.DIRECTORY_PORT)
                        .build();
    }

    @Override
    @SuppressWarnings("checkstyle:magicnumber")
    public void run() {
        log.debug("Connected client");
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            String line;
            while ((line = in.readLine()) != null) {
                log.trace("Received line " + line);
                String[] args = line.split(" ");
                switch (args[0]) {
                    case "new":
                        log.debug("New user");
                        district.addUser(args[1]);
                        out.println("User added to district: " + district.getName());
                        log.trace("Sent!");
                        break;
                    case "get_private":
                        log.debug("Sending private port " + district.getPrivatePubPort());
                        out.println("private_port " + district.getPrivatePubPort());
                        log.trace("Sent!");
                        break;
                    case "location":
                        log.debug("New location");
                        var x = Integer.parseInt(args[2]);
                        var y = Integer.parseInt(args[3]);
                        district.updateUserLocation(args[1], new Location(x, y));
                        var crowding = district.getPeopleOnLocation(x, y);
                        directory.updateLocation(
                                new covidtracker.api.Location(district.getName(), x, y, crowding));
                        out.println("Location updated!");
                        log.trace("Sent!");
                        break;
                    case "number":
                        log.debug("# of users requested");
                        int number =
                                district.getPeopleOnLocation(
                                        Integer.parseInt(args[1]), Integer.parseInt(args[2]));
                        out.println(
                                "There are "
                                        + number
                                        + " people on ("
                                        + Integer.parseInt(args[1])
                                        + ","
                                        + Integer.parseInt(args[2])
                                        + ")");
                        log.trace("Sent");
                        break;
                    case "sick":
                        log.debug("User sick");
                        district.notifyUserInfection(args[1]);
                        out.println("Infection added!");
                        log.trace("Sent");
                        break;
                    case "notifications":
                        log.debug("Pedido para receber notificações");
                        int port = district.getPublisherPort();
                        out.println("port " + port);
                        log.trace("Sent");
                        break;
                    default:
                        out.println("Unknown command!");
                }

                double contacted = 0;
                if (district.getNumInfected() != 0) {
                    contacted =
                            (double) district.getNumberOfContacted() / district.getNumInfected();
                }

                log.debug(
                        String.format(
                                "District: %s, Users: %s, Infected: %s, Contacted: %s, %s",
                                district.getName(),
                                district.getNumUsers(),
                                district.getNumInfected(),
                                district.getNumberOfContacted(),
                                contacted));

                directory.updateDistrict(
                        new covidtracker.api.District(
                                district.getName(),
                                district.getNumUsers(),
                                district.getNumInfected(),
                                contacted));
            }
        } catch (IOException | DuplicateUserException | NonExistentUserException e) {
            log.fatal(e.getMessage(), e);
        }
    }
}
