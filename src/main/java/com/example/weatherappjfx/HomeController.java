package com.example.weatherappjfx;

import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeController {
    public VBox checkboxContainer;
    List<Parameter> params = List.of(
            new Parameter("temperature_2m", "Temperatura"),
            new Parameter("B", "Testowy parametr 2")
    );

    private final Map<Parameter, CheckBox> checkBoxMap = new HashMap<>();

    @FXML
    public void initialize() {
        for (Parameter param : params) {
            CheckBox cb = new CheckBox(param.getDisplayName());
            checkboxContainer.getChildren().add(cb);
            checkBoxMap.put(param, cb);
        }
    }

    public List<String> getSelectedApiKeys() {
        return checkBoxMap.entrySet().stream()
                .filter(entry -> entry.getValue().isSelected())
                .map(entry -> entry.getKey().getApiKey())
                .toList();
    }

    @FXML
    private Label welcomeText;

    @FXML
    private TextField placeText;

    @FXML
    private DatePicker date;

    @FXML
    protected void onHelloButtonClick() {
        welcomeText.setText("Welcome to JavaFX Application!");
    }
}