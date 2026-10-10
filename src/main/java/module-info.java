module com.moafogaus {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires java.prefs;

    opens com.moafogaus to javafx.fxml;
    opens com.moafogaus.controller to javafx.fxml;
    opens com.moafogaus.model to javafx.fxml, javafx.base;

    exports com.moafogaus;
    exports com.moafogaus.controller;
    exports com.moafogaus.model;
    exports com.moafogaus.service;
}

