package covidtracker.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableView;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

import covidtracker.api.District;
import covidtracker.api.Location;
import covidtracker.client.HttpClient;
import covidtracker.util.Constants;

public final class Statistics implements Initializable {
    private HttpClient http;

    @FXML private TableView<Location> locationsTable;

    @FXML private ComboBox<String> locationsCombo;

    @FXML private Spinner<Integer> spinnerX;

    @FXML private Spinner<Integer> spinnerY;

    @FXML private TableView<District> districtsTable;

    @FXML private ComboBox<String> districtsCombo;

    private PrintWriter outPW;
    private TextArea terminal;
    private VBox mapa;

    public void loadVars(final PrintWriter out, final TextArea terminal, final VBox mapa) {
        this.terminal = terminal;
        this.outPW = out;
        this.mapa = mapa;
    }

    @FXML
    public void getDistrictStats(final ActionEvent event) {
        var district = http.getDistrict(districtsCombo.getValue());
        districtsTable.getItems().clear();
        districtsTable.getItems().add(district);
    }

    @FXML
    public void getTopDistricts(final ActionEvent event) {
        var districts = http.getDistricts();
        districtsTable.getItems().clear();
        districtsTable.getItems().addAll(districts);
    }

    @FXML
    public void getTopLocations(final ActionEvent event) {
        var locations = http.getLocations();
        locationsTable.getItems().clear();
        locationsTable.getItems().addAll(locations);
    }

    @FXML
    public void getLocationStats(final ActionEvent event) {
        var x = spinnerX.getValue();
        var y = spinnerY.getValue();
        var name = locationsCombo.getValue();

        var id = new Location(name, x, y).getID();
        var location = http.getLocation(id);

        locationsTable.getItems().clear();
        locationsTable.getItems().add(location);
    }

    @FXML
    public void goBack(final ActionEvent evt) throws IOException {
        Stage stage = (Stage) ((Node) evt.getSource()).getScene().getWindow();

        if (Constants.isLogged()) {
            FXMLLoader loader =
                    new FXMLLoader(getClass().getResource("../../views/dashboard.fxml"));
            Scene s = new Scene(loader.load());
            Dashboard l = loader.getController();
            l.initialize(null, null);
            l.loadSockets(this.outPW, this.terminal);
            l.setMapa(this.mapa);
            // l.fillPrevRect(lastRec);
            stage.setScene(s);

        } else {
            Scene scene =
                    new Scene(FXMLLoader.load(getClass().getResource("../../views/welcome.fxml")));
            stage.setScene(scene);
        }

        stage.show();
    }

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
        this.districtsCombo.getItems().addAll(Constants.getDistritos());
        this.locationsCombo.getItems().addAll(Constants.getDistritos());

        var hostname =
                Optional.ofNullable(System.getenv("DIRECTORY_SERVER_HOSTNAME")).orElse("127.0.0.1");
        var port =
                Integer.parseInt(
                        Optional.ofNullable(System.getenv("DIRECTORY_SERVER_PORT")).orElse("9090"));

        this.http = new HttpClient.Builder().hostname(hostname).port(port).build();
    }
}
