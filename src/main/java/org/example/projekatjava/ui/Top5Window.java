package org.example.projekatjava.ui;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.projekatjava.model.Country;
import org.example.projekatjava.model.Departure;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;

/**
 * Pomoćna klasa za otvaranje prozora “Top 5 ruta”.
 * <p>Učitava {@code top5-view.fxml}, primjenjuje CSS i poziva {@link Top5Controller#initData}.</p>
 */
public class Top5Window {

    /**
     * Prikazuje prozor sa Top 5 ruta.
     *
     * @param c     država (mapa gradova)
     * @param g     graf polazaka (stanica → polasci)
     * @param start početni grad (ID)
     * @param goal  odredišni grad (ID)
     * @param crit  kriterijum ("vrijeme", "cijena" ili "presjedanje")
     * @throws IOException ako FXML ili CSS ne mogu da se učitaju
     */
    public static void show(Country c,
                            Map<String, List<Departure>> g,
                            String start,
                            String goal,
                            String crit) throws IOException {
        FXMLLoader loader = new FXMLLoader(Top5Window.class.getResource("top5-view.fxml"));
        Stage stage = new Stage();
        Scene scene = new Scene(loader.load());
        URL css = HelloApplication.class.getResource("style.css");
        scene.getStylesheets().add(css.toExternalForm());
        stage.setScene(scene);
        stage.setTitle("Top 5 Ruta");

        Top5Controller controller = loader.getController();
        controller.initData(c, g, start, goal, crit);

        stage.show();
    }
}
