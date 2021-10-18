package covidtracker.controller;

import covidtracker.client.connection.ContactExterior;
import covidtracker.util.Constants;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public final class Welcome implements Initializable {
    private int frontEndPort;
    private PrintWriter out;

    @FXML private ComboBox<String> distrito;
    @FXML private TextField username;
    @FXML private PasswordField pass;
    @FXML private TextArea terminal;
    @FXML private Text errorConnect;

    private String getDistricto() {
        return this.distrito.getValue().replaceAll("\\s+", "_").toLowerCase();
    }

    @FXML
    public void gotoStatistics(final ActionEvent event) throws IOException {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene =
                new Scene(FXMLLoader.load(getClass().getResource("../../views/statistics.fxml")));
        stage.setScene(scene);
        stage.show();
    }

    @FXML
    public void registerUser() {
        out.println(
                "create " + username.getText() + " " + pass.getText() + " " + this.getDistricto());
        out.flush();
    }

    @FXML
    @SuppressWarnings("checkstyle:magicnumber")
    public void loginUser(final Event evt) {
        System.out.println("Stuff");
        try {
            Constants.setUsername(username.getText());
            Constants.setPassword(pass.getText());
            out.println(
                    "login "
                            + username.getText()
                            + " "
                            + pass.getText()
                            + " "
                            + this.getDistricto());
            out.flush();
            Thread.sleep(50);
            if (Constants.isLogged()) {
                Stage stage = (Stage) ((Node) evt.getSource()).getScene().getWindow();
                FXMLLoader loader =
                        new FXMLLoader(getClass().getResource("../../views/dashboard.fxml"));
                Scene s = new Scene(loader.load());
                Dashboard l = loader.getController();
                l.initialize(null, null);
                l.loadSockets(this.out, this.terminal);
                stage.setScene(s);
                stage.show();
            } else {
                terminal.setText(
                        "Couldn't login, connection may be slow or credentials are wrong!");
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initialize(final URL url, final ResourceBundle resourceBundle) {
        try {
            frontEndPort =
                    Integer.parseInt(
                            Optional.ofNullable(System.getenv("FRONT_END_SERVER_PORT"))
                                    .orElse("8080"));
            Socket toFrontEnd = new Socket("localhost", frontEndPort);
            BufferedReader in =
                    new BufferedReader(new InputStreamReader(toFrontEnd.getInputStream()));
            out = new PrintWriter(toFrontEnd.getOutputStream());
            new Thread(new ContactExterior(in, terminal)).start();
            distrito.getItems().addAll(Constants.getDistritos());
            distrito.setValue("Braga");
        } catch (IOException e) {
            errorConnect.setVisible(true);
            e.printStackTrace();
        }
    }
}
