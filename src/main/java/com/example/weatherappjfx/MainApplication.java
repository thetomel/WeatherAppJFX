package com.example.weatherappjfx;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.InetAddress;

public class MainApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        if (isInternetAvailable()) {
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("home-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1280, 720);
        stage.setTitle("WeatherFX");
        scene.getStylesheets().add(getClass().getResource("homeview.css").toExternalForm());
        stage.setScene(scene);
        stage.show();
        }
        else {
            showNoInternetConnectionDialog();
        }
    }

    private boolean isInternetAvailable() {
        try {
            InetAddress address = InetAddress.getByName("8.8.8.8");
            return address.isReachable(3000);
        } catch (Exception e) {
            return false;
        }
    }
    public void showNoInternetConnectionDialog() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("No Connection");
        alert.setHeaderText("Brak połączenia Internetowego");
        alert.setContentText("Aplikacja do działania potrzebuje połączenia internetowego. Sprawdź swoje połączenie i uruchom program ponowne");
        ButtonType button = new ButtonType("Zamknij", ButtonBar.ButtonData.OK_DONE);
        alert.getButtonTypes().setAll(button);

        alert.showAndWait().ifPresent(buttonType -> {
                if (buttonType == button){
                    Platform.exit();}
        });
    }
    public static void main(String[] args) {
        launch();
    }
}