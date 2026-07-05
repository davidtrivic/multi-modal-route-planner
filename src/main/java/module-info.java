module org.example.projekatjava {
    requires gs.core;
    requires gs.ui.javafx;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.swing;
    requires com.fasterxml.jackson.databind;

    exports org.example.projekatjava.generator;
    exports org.example.projekatjava.data;
    exports org.example.projekatjava.algorithm;
    exports org.example.projekatjava.model;
    exports org.example.projekatjava.ui;

    opens org.example.projekatjava.ui to javafx.graphics, javafx.fxml;
}