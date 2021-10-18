package covidtracker.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.util.Pair;

import org.zeromq.ZMQ;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import covidtracker.util.Constants;

public final class Dashboard implements Initializable {
    @FXML private VBox mapa;
    @FXML private HBox options;
    @FXML private Rectangle rect;
    @FXML private HBox informations;
    @FXML private ComboBox<String> d1, d2, d3;
    private PrintWriter outPW;
    private ZMQ.Socket subSocket;
    private boolean clickedRectBefore;
    private boolean quarantine;
    private Rectangle previosRect;
    private Map<String, VBox> subscribedDistricts;
    private Map<ComboBox<String>, Pair<Boolean, String>> subBefore;

    private Pair<Integer, Integer> getRectClicked(final MouseEvent e) {
        double mx = e.getSceneX(), my = e.getSceneY();
        double mapX = mx - mapa.getLayoutX(), mapY = my - mapa.getLayoutY();
        int rx, ry;
        rx = (int) (mapX / rect.getWidth());
        ry = (int) (mapY / rect.getHeight());
        return new Pair<>(rx, ry);
    }

    @FXML
    public void setInfected() {
        if (!quarantine) {
            quarantine = true;
            outPW.println("sick");
            outPW.flush();
        }
    }

    @FXML
    public void clearText() {
        TextArea out = (TextArea) informations.getChildren().get(0);
        out.setText("");
    }

    @SuppressWarnings("checkstyle:magicnumber")
    public VBox getNots(final String district) {
        String distrito = district.replaceAll("\\s+", "_").toLowerCase();
        VBox vbox = new VBox();
        vbox.setSpacing(10);
        vbox.setAlignment(Pos.CENTER);
        Text t = new Text(district);
        t.setTextAlignment(TextAlignment.CENTER);
        vbox.getChildren().add(t);
        CheckBox cb1 = new CheckBox();
        CheckBox cb2 = new CheckBox();
        CheckBox cb3 = new CheckBox();
        CheckBox cb4 = new CheckBox();
        cb1.setText("Empty Location");
        TextArea out = (TextArea) informations.getChildren().get(0);
        cb1.setOnAction(
                e -> {
                    if (!cb1.isSelected()) {
                        subSocket.unsubscribe("sub_empty " + distrito);
                        out.setText(out.getText() + "\nUnsubscribed empty!");
                    } else {
                        subSocket.subscribe("sub_empty " + distrito);
                        out.setText(out.getText() + "\nSubscribed empty!");
                    }
                });
        cb2.setOnAction(
                e -> {
                    if (!cb2.isSelected()) {
                        subSocket.unsubscribe("sub_more " + distrito);
                        out.setText(out.getText() + "\nUnsubscribed higher concentration!");
                    } else {
                        subSocket.subscribe("sub_more " + distrito);
                        out.setText(out.getText() + "\nSubscribed higher concentration!");
                    }
                });
        cb3.setOnAction(
                e -> {
                    if (!cb3.isSelected()) {
                        subSocket.unsubscribe("sub_less " + distrito);
                        out.setText(out.getText() + "\nUnsubscribed lower concentration!");
                    } else {
                        subSocket.subscribe("sub_less " + distrito);
                        out.setText(out.getText() + "\nSubscribed lower concentration!");
                    }
                });
        cb4.setOnAction(
                e -> {
                    if (!cb4.isSelected()) {
                        subSocket.unsubscribe("sub_infected " + distrito);

                        out.setText(out.getText() + "\nUnsubscribed infections!");
                    } else {
                        subSocket.subscribe("sub_infected " + distrito);
                        out.setText(out.getText() + "\nSubscribed infections!");
                    }
                });
        cb2.setText("High Concentration");
        cb3.setText("Low Concentration");
        cb4.setText("Infections");
        vbox.getChildren().addAll(cb1, cb2, cb3, cb4);
        return vbox;
    }

    @SuppressWarnings("checkstyle:magicnumber")
    public void changeLocation(final MouseEvent e) {
        if (!quarantine) {
            Pair<Integer, Integer> mc = getRectClicked(e);
            if (e.getButton() == MouseButton.PRIMARY) {
                if (clickedRectBefore) {
                    previosRect.setFill(new Color(214.0 / 255, 207.0 / 255, 165.0 / 255, 1));
                }
                Rectangle r = (Rectangle) (e.getSource());
                clickedRectBefore = true;
                r.setFill(new Color(0.1, 0.1, 0.5, 0.4));
                outPW.println("change_location " + mc.getKey() + " " + mc.getValue());
                outPW.flush();
                previosRect = r;
                System.out.println(mc.getKey() + ", " + mc.getValue());
            } else if (e.getButton() == MouseButton.SECONDARY) {
                outPW.println("get_people " + mc.getKey() + " " + mc.getValue());
                outPW.flush();
            }
        } else {
            TextArea out = (TextArea) informations.getChildren().get(0);
            out.setText(out.getText() + "\nYou are in quarantine, dont move!");
        }
    }

    public void loadSockets(final PrintWriter out, final TextArea terminal) {
        this.subSocket = Constants.getSubSocket();
        informations.getChildren().add(terminal);
        this.outPW = out;
    }

    @SuppressWarnings("checkstyle:magicnumber")
    public void subscribeToDistrict(final ActionEvent e) {
        ComboBox<String> c = (ComboBox<String>) e.getSource();
        String nd = c.getValue();
        if (!subscribedDistricts.containsKey(nd)) {
            System.out.println("entrou aqui");
            if (subBefore.get(c).getKey()) {
                options.getChildren().remove(subscribedDistricts.get(subBefore.get(c).getValue()));
                subscribedDistricts.remove(subBefore.get(c).getValue());
                outPW.println(
                        "get_nots "
                                + subBefore
                                        .get(c)
                                        .getValue()
                                        .replaceAll("\\s+", "_")
                                        .toLowerCase());
                outPW.flush();
                try {
                    Thread.sleep(50);
                } catch (InterruptedException interruptedException) {
                    interruptedException.printStackTrace();
                }
            }
            subBefore.put(c, new Pair<>(true, nd));
            VBox nvbox = getNots(nd);
            subscribedDistricts.put(nd, nvbox);
            options.getChildren().add(nvbox);
            outPW.println("get_nots " + nd.replaceAll("\\s+", "_").toLowerCase());
            outPW.flush();
        }
    }

    public void setMapa(VBox mapa) {
        this.mapa = mapa;
    }

    public void fillPrevRect(Rectangle rec) {
        rec.setFill(new Color(0.1, 0.1, 0.5, 0.4));
    }

    @FXML
    public void gotoStatistics(final ActionEvent evt) throws IOException {
        Stage stage = (Stage) ((Node) evt.getSource()).getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("../../views/statistics.fxml"));
        Scene s = new Scene(loader.load());
        Statistics l = loader.getController();
        l.initialize(null, null);
        l.loadVars(this.outPW, (TextArea) informations.getChildren().get(0), this.mapa);
        stage.setScene(s);
        stage.show();
    }

    @FXML
    public void logout(final ActionEvent event) throws IOException {
        // TODO: @rui enviar mensagem de logout
        // outPW.println("logout " + Constants.getUsername() + " " + Constants.getPassword() + " "
        // );
        Constants.setLogged(false);
        Constants.setUsername(null);
        Constants.setPassword(null);

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene =
                new Scene(FXMLLoader.load(getClass().getResource("../../views/welcome.fxml")));
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void initialize(final URL url, final ResourceBundle resourceBundle) {
        this.subscribedDistricts = new HashMap<>();
        this.subBefore = new HashMap<>();
        subBefore.put(d1, new Pair<>(false, ""));
        subBefore.put(d2, new Pair<>(false, ""));
        subBefore.put(d3, new Pair<>(false, ""));
        d1.getItems().addAll(Constants.getDistritos());
        d2.getItems().addAll(Constants.getDistritos());
        d3.getItems().addAll(Constants.getDistritos());
        clickedRectBefore = false;
    }
}
