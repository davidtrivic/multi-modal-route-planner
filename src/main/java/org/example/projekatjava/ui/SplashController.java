package org.example.projekatjava.ui;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.stage.Stage;
import org.example.projekatjava.data.LoadRacuni;

import java.net.URL;

/**
 * Kontroler splash ekrana.
 * <ul>
 *   <li>Animira progress bar ravnomjerno 5s,</li>
 *   <li>u pozadini učitava brojač računa i ukupni prihod,</li>
 *   <li>kada su i animacija i task gotovi – otvara glavni prozor.</li>
 * </ul>
 */
public class SplashController {
    /** Progres bar splash ekrana. */
    public ProgressBar progressBar;
    /** Poruka statusa. */
    public Label statusLabel;
    /** Prikaz broja računa i prihoda. */
    public Label countLabel, revenueLabel;

    private volatile boolean taskDone = false;
    private volatile boolean animDone = false;

    /**
     * Inicijalizacija: dodaje CSS, pokreće glatku animaciju i Task za učitavanje podataka.
     */
    public void initialize() {
        Platform.runLater(() -> {
            var scene = progressBar.getScene();
            if (scene != null) {
                URL css = getClass().getResource("style.css");
                if (css == null) css = getClass().getResource("/org/example/projekatjava/ui/style.css");
                if (css != null) scene.getStylesheets().add(css.toExternalForm());
            }
        });

        javafx.beans.property.DoubleProperty anim = new javafx.beans.property.SimpleDoubleProperty(0);
        progressBar.progressProperty().bind(anim);

        javafx.animation.Timeline tl = new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(javafx.util.Duration.ZERO,
                        new javafx.animation.KeyValue(anim, 0, javafx.animation.Interpolator.LINEAR)),
                new javafx.animation.KeyFrame(javafx.util.Duration.seconds(5),
                        new javafx.animation.KeyValue(anim, 1, javafx.animation.Interpolator.LINEAR))
        );
        tl.setOnFinished(e -> { animDone = true; maybeFinish(); });
        tl.play();

        Task<LoadRacuni> loadTask = new Task<>() {
            @Override
            protected LoadRacuni call() {
                updateMessage("Učitavam podatke…");
                return new LoadRacuni();
            }
        };

        if (statusLabel != null) statusLabel.textProperty().bind(loadTask.messageProperty());

        loadTask.setOnSucceeded(ev -> {
            var lr = loadTask.getValue();
            countLabel.setText(String.valueOf(lr.getBrojRacuna()));
            revenueLabel.setText(String.valueOf(lr.getUkupanPrihod()));
            statusLabel.textProperty().unbind();
            statusLabel.setText("Dobrodošli");
            taskDone = true;
            maybeFinish();
        });

        loadTask.setOnFailed(ev -> {
            countLabel.setText("0");
            revenueLabel.setText("0");
            statusLabel.textProperty().unbind();
            statusLabel.setText("Greška pri učitavanju.");
            taskDone = true;
            maybeFinish();
        });

        Thread t = new Thread(loadTask, "racuni-loader");
        t.setDaemon(true);
        t.start();
    }

    /**
     * Zatvara splash tek kada su i animacija (5s) i task gotovi.
     */
    private void maybeFinish() {
        if (taskDone && animDone) {
            openMainAndCloseSplash();
        }
    }

    /**
     * Otvara glavni prozor i zatvara splash.
     */
    private void openMainAndCloseSplash() {
        try {
            FXMLLoader mainLoader = new FXMLLoader(getClass().getResource("hello-view.fxml"));
            Stage main = new Stage();
            Scene scene = new Scene(mainLoader.load());

            var css = getClass().getResource("style.css");
            if (css == null) css = getClass().getResource("/org/example/projekatjava/ui/style.css");
            if (css != null) scene.getStylesheets().add(css.toExternalForm());

            main.setScene(scene);
            main.setTitle("Aplikacija");
            main.setResizable(false);
            main.show();

            ((Stage) progressBar.getScene().getWindow()).close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
