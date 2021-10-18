package covidtracker;

import java.io.IOException;
import java.util.Optional;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class App extends Application {
    private static Logger log = LogManager.getLogger(App.class);

    private static final String PROGRAM_NAME = "Covid Tracker";

    @Parameter(
            names = {"-h", "--help"},
            help = true,
            description = "Displays help information")
    private boolean help = false;

    @Parameter(
            names = {"-fep", "--front-end-port"},
            description = "Port used to contact frontEnd server")
    private int frontEndPort =
            Integer.parseInt(
                    Optional.ofNullable(System.getenv("FRONT_END_SERVER_PORT")).orElse("8080"));

    public App() {}

    public static void main(final String[] args) {
        launch(args);
    }

    @Override
    public void init() throws Exception {
        super.init();

        var args = getParameters().getRaw().toArray(new String[0]);
        this.parseArguments(args);
    }

    @Override
    @SuppressWarnings("checkstyle:FinalParameters")
    public void start(Stage stage) throws IOException {
        var start = new Scene(FXMLLoader.load(getClass().getResource("../views/welcome.fxml")));
        stage.setTitle(PROGRAM_NAME);
        stage.setScene(start);
        stage.show();
    }

    private void parseArguments(final String[] args) {
        var commands = new JCommander(this);
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
