package com.moafogaus;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * SoundWave Converter - Application de Conversion Audio & Vidéo JavaFX
 */
public class App extends Application {

    private static Scene scene;

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("/com/moafogaus/main-view.fxml"));
        Parent root = fxmlLoader.load();

        scene = new Scene(root, 980, 700);
        
        stage.setTitle("SoundWave Converter - Audio & Vidéo");
        stage.setMinWidth(900);
        stage.setMinHeight(650);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}