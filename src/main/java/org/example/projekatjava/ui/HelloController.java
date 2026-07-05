package org.example.projekatjava.ui;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import org.example.projekatjava.algorithm.BFS;
import org.example.projekatjava.algorithm.DijkstraPrice;
import org.example.projekatjava.algorithm.DijkstraTime;
import org.example.projekatjava.algorithm.Result;
import org.example.projekatjava.data.LoadData;
import org.example.projekatjava.model.City;
import org.example.projekatjava.model.Country;
import org.example.projekatjava.model.Departure;
import org.example.projekatjava.model.Racun;

import java.net.URL;
import java.util.*;

/**
 * Glavni kontroler UI-a.
 * <ul>
 *   <li>Učitava podatke i puni combo box-eve gradova,</li>
 *   <li>poziva algoritme (vrijeme/cijena/presjedanja) i puni tabelu rezultata,</li>
 *   <li>otvara Top 5 prozor,</li>
 *   <li>generiše račun za selektovanu rutu.</li>
 * </ul>
 */
public class HelloController implements Initializable {

    /** UI elementi injektovani preko FXML-a. */
    @FXML public Label label1, label2, label3, totalLabel;
    @FXML public ComboBox<String> combo1, combo2;
    @FXML public RadioButton radioButton1, radioButton2, radioButton3;
    @FXML public Button rutaButton, top5Button, buyButton;
    @FXML public ToggleGroup criteriumGroup = new ToggleGroup();
    @FXML public TableView<Departure> routeTable;
    @FXML public TableColumn<Departure, String> polazak, dolazak, tip;
    @FXML public TableColumn<Departure, Number> cijena;

    /** Shared podaci/graf i algoritmi. */
    public static final org.example.projekatjava.graph.GraphStream graphStream = new org.example.projekatjava.graph.GraphStream();
    public static final Country country = new Country();
    public static final LoadData ld = new LoadData();
    public static Map<String, List<Departure>> departuresGraph;

    /** Trenutni kriterijum i stanje grafa. */
    public String criterium = "";
    private boolean graphInitialized = false;

    /** Algoritmi. */
    public static DijkstraPrice dijkstraPrice = new DijkstraPrice();
    public static DijkstraTime dijkstraTime = new DijkstraTime();
    public static BFS bfs = new BFS();

    /** Posljednji rezultat za kupovinu. */
    private Result lastResult;

    /**
     * Inicijalizuje UI: učitava podatke, postavlja combo liste i kolone tabele, te inicijalizuje graf.
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle){
        radioButton1.setToggleGroup(criteriumGroup);
        radioButton2.setToggleGroup(criteriumGroup);
        radioButton3.setToggleGroup(criteriumGroup);

        // UserData na radio dugmadima olaksava citanje kriterijuma
        radioButton1.setUserData("vrijeme");
        radioButton2.setUserData("cijena");
        radioButton3.setUserData("presjedanje");

        try {
            if (departuresGraph == null) {
                departuresGraph = ld.loadData(country);
            }
            if (!graphInitialized) {
                graphStream.buildFrom(departuresGraph);
                graphStream.showGraph();
                graphInitialized = true;
            }

            City[][] map = country.getCountryMap();
            if (map == null) return;

            List<String> cityNames = new ArrayList<>();
            for (City[] row : map) for (City c : row) if (c != null) cityNames.add(c.getName());
            combo1.setItems(FXCollections.observableArrayList(cityNames));
            combo2.setItems(FXCollections.observableArrayList(cityNames));

            polazak.setCellValueFactory(c ->
                    new javafx.beans.property.SimpleStringProperty(c.getValue().getFrom() + " (" + c.getValue().getDepartureTime() + ")"));
            dolazak.setCellValueFactory(c -> {
                Departure d = c.getValue();
                String arr = hhmmPlusMinutes(d.getDepartureTime(), d.getDuration());
                return new javafx.beans.property.SimpleStringProperty(d.getTo() + " (" + arr + ")");
            });
            tip.setCellValueFactory(c ->
                    new javafx.beans.property.SimpleStringProperty(c.getValue().getType().equalsIgnoreCase("autobus") ? "Autobus" : "Voz"));
            cijena.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getPrice()));

            routeTable.setVisible(false);
            routeTable.setManaged(false);
            totalLabel.setVisible(false);
            totalLabel.setManaged(false);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Klik na “Prikaži rutu”: čita kriterijum, validira ulaz i prikazuje najbolju rutu uz highlight na grafu.
     */
    public void rutaButtonClicked(ActionEvent actionEvent)  throws Exception{
        Toggle sel = criteriumGroup.getSelectedToggle();
        if (sel == null) { ErrorWindow.show(); return; }

        criterium = String.valueOf(sel.getUserData());
        String startCity = combo1.getValue(), goalCity = combo2.getValue();
        if (startCity == null || goalCity == null || startCity.equals(goalCity)) { ErrorWindow.show(); return; }

        if (!graphInitialized) {
            graphStream.buildFrom(departuresGraph);
            graphStream.showGraph();
            graphInitialized = true;
        }

        graphStream.clearHighlight();

        Result res = switch (criterium) {
            case "vrijeme"     -> dijkstraTime.dijkstraMinTime(country, departuresGraph, startCity, goalCity);
            case "cijena"      -> dijkstraPrice.dijkstraMinPrice(departuresGraph, startCity, goalCity);
            case "presjedanje" -> bfs.bfsMinTransfer(departuresGraph, startCity, goalCity);
            default            -> null;
        };

        if (res != null && res.getPath() != null && !res.getPath().isEmpty()) {
            graphStream.highlightRoute(res.getPath());
            showRouteInTable(res);
            lastResult = res;
        } else {
            ErrorWindow.show();
        }
    }

    /**
     * Ručno mapira tekst radio dugmeta na string kriterijuma.
     */
    public void getCriterium(ActionEvent event){
        RadioButton selected = (RadioButton) criteriumGroup.getSelectedToggle();
        if (selected != null) {
            if(selected.getText().equals("Najkrace vrijeme putovanja"))      criterium = "vrijeme";
            else if(selected.getText().equals("Najniza cijena"))              criterium = "cijena";
            else if(selected.getText().equals("Najmanji broj presjedanja"))   criterium = "presjedanje";
        }
    }

    /**
     * Pomoćna metoda: računa HH:mm + addMin (sa promjenom dana).
     *
     * @param depHHmm polazak u formatu HH:mm
     * @param addMin  dodati minuti
     * @return formatirano HH:mm
     */
    public static String hhmmPlusMinutes(String depHHmm, int addMin) {
        int h = Integer.parseInt(depHHmm.substring(0, 2));
        int m = Integer.parseInt(depHHmm.substring(3, 5));
        int total = h * 60 + m + addMin;
        int hh = ((total / 60) % 24 + 24) % 24;
        int mm = (total % 60 + 60) % 60;
        return String.format("%02d:%02d", hh, mm);
    }

    /**
     * Popunjava tabelu rezultatima i prikazuje ukupno vrijeme/cijenu.
     *
     * @param res rezultat rute
     */
    private void showRouteInTable(Result res) {
        if (res == null || res.getPath() == null || res.getPath().isEmpty()) {
            routeTable.getItems().clear();
            totalLabel.setText("Nema dostupne rute.");
            routeTable.setVisible(true); routeTable.setManaged(true);
            totalLabel.setVisible(true); totalLabel.setManaged(true);
            return;
        }

        routeTable.getItems().setAll(res.getPath());

        long totalMin = res.totalTravelMinutes();
        long h = totalMin / 60, m = totalMin % 60;
        int totalPrice = res.getTotalPrice();
        totalLabel.setText(String.format("Ukupno: %dh %dmin, %d novčanih jedinica.", h, m, totalPrice));

        routeTable.setVisible(true); routeTable.setManaged(true);
        totalLabel.setVisible(true); totalLabel.setManaged(true);
    }

    /**
     * Klik na “Kupi kartu”: generiše račun iz posljednjeg rezultata i prikazuje stilizovani alert.
     */
    public void buyButtonClicked(ActionEvent actionEvent) {
        if (lastResult == null || lastResult.getPath() == null || lastResult.getPath().isEmpty()) {
            ErrorWindow.show(); return;
        }

        Racun racun = new Racun(lastResult);
        racun.generisiRacun();

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setContentText("Karta je uspješno kupljena!");

        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
        dialogPane.getStyleClass().add("custom-alert");

        alert.showAndWait();
    }

    /**
     * Otvara prozor “Top 5 ruta” na osnovu izabranog kriterijuma i gradova.
     */
    public void top5ButtonClicked(ActionEvent event) throws Exception {
        if (criterium.isBlank()) { ErrorWindow.show(); return; }
        String startCity = combo1.getValue(), goalCity = combo2.getValue();
        if (startCity == null || goalCity == null || startCity.equals(goalCity)) { ErrorWindow.show(); return; }

        Toggle sel = criteriumGroup.getSelectedToggle();
        if (sel == null) { ErrorWindow.show(); return; }

        criterium = String.valueOf(sel.getUserData());
        Top5Window.show(country, departuresGraph, startCity, goalCity, criterium);
    }
}
