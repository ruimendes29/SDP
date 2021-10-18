package covidtracker;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Optional;
import java.util.Scanner;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;

import covidtracker.client.HttpClient;
import covidtracker.util.Config;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import covidtracker.model.District;
import covidtracker.server.ServerWorker;
import covidtracker.util.Parser;

public final class App {
    private static Logger log = LogManager.getLogger(App.class);

    private static final String PROGRAM_NAME = "district-server";

    @Parameter(
            names = {"-h", "--help"},
            help = true,
            description = "Displays help information")
    private boolean help = false;

    @Parameter(
            names = {"-a", "--address", "--hostname"},
            description = "Hostname address")
    private String address =
            Optional.ofNullable(System.getenv("APP_SERVER_HOSTNAME")).orElse("127.0.0.1");

    @Parameter(
            names = {"-n", "--name"},
            required = true,
            description = "District name")
    private String name;

    private int port;
    private int p1, p2, p3, p4;

    private App() {}

    public static void main(final String[] args) {
        new App().start(args);
    }

    @SuppressWarnings("checkstyle:magicnumber")
    public void start(final String[] args) {
        this.parseArguments(args);

        Parser.readFile("target/classes" + "/art/logo.ascii").stream().forEach(System.out::println);
        log.info("Application started successfully");

        try {
            File myObj = new File("target/classes/mapDistrictToPort.txt");
            Scanner myReader = new Scanner(myObj);
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                log.debug(data);
                String[] infos = data.split(" ");
                if (infos[0].equals(name.toLowerCase())) {
                    this.port = Integer.parseInt(infos[1]);
                    this.p1 = Integer.parseInt(infos[2]);
                    this.p2 = Integer.parseInt(infos[3]);
                    this.p3 = Integer.parseInt(infos[4]);
                    this.p4 = Integer.parseInt(infos[5]);
                    break;
                }
            }
            myReader.close();
        } catch (FileNotFoundException e) {
            log.error(e.getMessage(), e);
        }

        try {
            District district = new District(name, p1, p2, p3, p4);

            new HttpClient.Builder()
                    .hostname(Config.DIRECTORY_HOST)
                    .port(Config.DIRECTORY_PORT)
                    .build()
                    .createDistrict(new covidtracker.api.District(name));

            ServerSocket server = new ServerSocket();
            server.bind(new InetSocketAddress(this.address, this.port));
            log.info(
                    "District Server ("
                            + this.name
                            + ") is up at "
                            + server.getLocalSocketAddress());

            while (true) {
                Socket client = server.accept();
                new Thread(new ServerWorker(client, district)).start();
            }
        } catch (Exception e) {
            log.fatal(e.getMessage(), e);
        }
    }

    public void parseArguments(final String[] args) {
        JCommander commands = new JCommander(this);
        commands.setProgramName(PROGRAM_NAME);

        try {
            commands.parse(args);
        } catch (ParameterException exception) {
            System.err.println(exception.getMessage());
            commands.usage();
            System.exit(1);
        }

        if (this.help) {
            commands.usage();
            System.exit(0);
        }
    }
}
