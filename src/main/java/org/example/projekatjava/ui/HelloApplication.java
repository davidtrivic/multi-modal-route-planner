package org.example.projekatjava.ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;

/**
 * Ulazna tačka JavaFX aplikacije.
 * <p>Pokreće splash ekran i globalno dodaje {@code style.css} za dizajn prozora.</p>
 */
public class HelloApplication extends Application {

    /**
     * Inicijalizuje i prikazuje splash prozor.
     *
     * @param stage primarni {@link Stage} koji se koristi za splash
     * @throws Exception ako učitavanje FXML ili CSS resursa ne uspije
     */
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader splashLoader = new FXMLLoader(getClass().getResource("splash-view.fxml"));
        Scene scene = new Scene(splashLoader.load(), 680, 530);
        URL css = HelloApplication.class.getResource("style.css");

        scene.getStylesheets().add(css.toExternalForm());
        stage.setTitle("Učitavanje...");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    /**
     * Standardni JavaFX ulaz. Prosljeđuje kontrolu platformi.
     *
     * @param args argumenti komandne linije (ne koriste se)
     * @throws Exception ako start ne uspije
     */
    public static void main(String[] args) throws Exception {
        launch();
    }
}
