package org.example.projekatjava.ui;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.net.URL;

/**
 * Modalni prozor za prikaz greške.
 * <p>Učitava {@code error-view.fxml} i primjenjuje globalni CSS.</p>
 */
public class ErrorWindow {

    /**
     * Prikaže modalni prozor sa porukom greške.
     * <p>Blokira glavni prozor dok je otvoren ({@link Modality#APPLICATION_MODAL}).</p>
     */
    public static void show() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(ErrorWindow.class.getResource("error-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 300, 200);
            Stage stage = new Stage();
            URL css = HelloApplication.class.getResource("style.css");

            scene.getStylesheets().add(css.toExternalForm());
            stage.setTitle("Greška");
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            stage.show();
        } catch(Exception e){
            e.printStackTrace();
        }
    }
}
