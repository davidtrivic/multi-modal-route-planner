package org.example.projekatjava.ui;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.example.projekatjava.algorithm.Result;
import org.example.projekatjava.algorithm.Top5Routes;
import org.example.projekatjava.model.Country;
import org.example.projekatjava.model.Departure;
import org.example.projekatjava.model.Racun;

import java.util.List;
import java.util.Map;

/**
 * Kontroler prozora “Top 5 ruta”.
 * <ul>
 *   <li>Računa top K ruta po zadatom kriterijumu,</li>
 *   <li>prikazuje rute i omogućava prebacivanje između njih,</li>
 *   <li>generiše račun za trenutno prikazanu rutu.</li>
 * </ul>
 */
public class Top5Controller {
    /** Dugmad za izbor 5 kandidovanih ruta. */
    @FXML public ToggleButton routeBtn1, routeBtn2, routeBtn3, routeBtn4, routeBtn5;
    /** Naslovni label. */
    @FXML public Label titleLabel;
    /** Tabela prikaza dionica izabrane rute. */
    @FXML public TableView<Departure> routesTable;
    @FXML public TableColumn<Departure, String> polazak, dolazak, tip;
    @FXML public TableColumn<Departure, Number> cijena;
    /** Sažetak: vrijeme i cijena rute. */
    @FXML public Label summaryLabel;
    /** Dugme “Kupi kartu”. */
    @FXML public Button buyButton;
    /** Grupa Toggle Button-a za rute. */
    @FXML private ToggleGroup routeGroup;

    private List<Top5Routes.Route> routes;
    private Result currentResult;

    /**
     * Inicijalizuje sadržaj prozora: računa top 5, postavlja kolone i listener za izbor rute.
     *
     * @param country   model države (mreža gradova)
     * @param graph     graf polazaka (stanica → lista polazaka)
     * @param startCity početni grad
     * @param goalCity  ciljni grad
     * @param criterium kriterijum ("vrijeme", "cijena", "presjedanje")
     */
    public void initData(Country country, Map<String, List<Departure>> graph,
                         String startCity, String goalCity, String criterium) {
        polazak.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getFrom() + " (" + c.getValue().getDepartureTime() + ")"));
        dolazak.setCellValueFactory(c -> {
            Departure d = c.getValue();
            String arr = HelloController.hhmmPlusMinutes(d.getDepartureTime(), d.getDuration());
            return new SimpleStringProperty(d.getTo() + " (" + arr + ")");
        });
        tip.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getType().equalsIgnoreCase("autobus") ? "Autobus" : "Voz"));
        cijena.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getPrice()));

        titleLabel.setText("Top 5 ruta: " + startCity + " -> " + goalCity + ", kriterijum: " + criterium);
        titleLabel.setAlignment(javafx.geometry.Pos.CENTER);

        Top5Routes.Criterion critEnum =
                "vrijeme".equals(criterium) ? Top5Routes.Criterion.TIME :
                        "cijena".equals(criterium)  ? Top5Routes.Criterion.PRICE :
                                Top5Routes.Criterion.TRANSFERS;

        Top5Routes top5 = new Top5Routes();
        routes = top5.topK(country, graph, startCity, goalCity, critEnum, 5);

        routeGroup.selectedToggleProperty().addListener((obs, oldT, newT) -> {
            if (newT instanceof ToggleButton tb && tb.getUserData() != null) {
                try {
                    int idx = Integer.parseInt(tb.getUserData().toString());
                    showRoute(idx);
                } catch (NumberFormatException ignored) {}
            }
        });

        if (routes != null && !routes.isEmpty()) {
            showRoute(0);
            routeBtn1.setSelected(true);
        } else {
            routesTable.getItems().clear();
            summaryLabel.setText("Nije pronađena nijedna ruta.");
            buyButton.setDisable(true);
        }
    }

    /**
     * Prikazuje rutu sa indeksom {@code idx} u tabeli i ažurira sažetak.
     */
    private void showRoute(int idx) {
        if (routes == null || idx < 0 || idx >= routes.size()) return;
        Result r = routes.get(idx).result;

        currentResult = r;
        routesTable.getItems().setAll(r.getPath());

        long totalMin = r.totalTravelMinutes();
        long h = totalMin / 60, m = totalMin % 60;
        summaryLabel.setText(String.format("Ukupno: %dh %dmin, %d novčanih jedinica.",
                h, m, r.getTotalPrice()));
        buyButton.setDisable(r.getPath() == null || r.getPath().isEmpty());
    }

    /**
     * Klik na “Kupi kartu” za trenutno selektovanu rutu.
     * <p>Kreira i upisuje račun na disk, pa prikazuje informativni alert.</p>
     */
    @FXML
    private void buyButtonClicked() {
        if (currentResult == null || currentResult.getPath().isEmpty()) return;
        Racun racun = new Racun(currentResult);
        racun.generisiRacun();

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setContentText("Karta je uspješno kupljena!");

        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
        dialogPane.getStyleClass().add("custom-alert");

        alert.showAndWait();
    }
}
