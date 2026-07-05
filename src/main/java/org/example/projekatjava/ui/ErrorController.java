package org.example.projekatjava.ui;

import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

/**
 * Kontroler za modalni prozor greške.
 * <p>Sadrži jedno dugme za zatvaranje dijaloga.</p>
 */
public class ErrorController {
    /** Poruka greške. */
    public Label label1;
    /** Dugme “U redu”. */
    public Button button1;

    /**
     * Zatvara prozor greške.
     */
    public void button1Clicked(ActionEvent actionEvent) {
        Stage stage = (Stage) button1.getScene().getWindow();
        stage.close();
    }
}
