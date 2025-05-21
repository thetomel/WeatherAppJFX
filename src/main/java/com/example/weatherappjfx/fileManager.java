package com.example.weatherappjfx;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class fileManager {
    public void writeToFile(String fileName, String content) {
        String userDocumentsPath = System.getProperty("user.home") + File.separator + "Documents";
        File documentsDir = new File(userDocumentsPath);
        if (!documentsDir.exists()) {
            documentsDir.mkdirs();
        }
        try{
            File file = new File(userDocumentsPath + File.separator + fileName  + ".txt");
            FileWriter fw = new FileWriter(file);
            fw.write(content);
            fw.close();
            System.out.println(userDocumentsPath + File.separator + fileName + ".txt");
    }
    catch (IOException e){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Problem z zapisem do pliku");
        alert.setHeaderText("Dane nie mogą zostać zapisane");
        alert.setContentText("Aplikacja nie była w stanie wykonać zapisu danych");
        ButtonType button = new ButtonType("Zamknij", ButtonBar.ButtonData.OK_DONE);
        alert.getButtonTypes().setAll(button);

        alert.showAndWait().ifPresent(buttonType -> {
            if (buttonType == button){
                Platform.exit();}
        });
        throw new RuntimeException(e);
        }
    }
}
