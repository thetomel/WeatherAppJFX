package com.example.weatherappjfx;

import com.example.weatherappjfx.api.ApiController;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeController {
    private ApiController weatherBox;
    public VBox checkboxContainer;
    List<Parameter> params = List.of(
            new Parameter("temperature_2m", "Temperatura"),
            new Parameter("B", "Testowy parametr 2")
    );

    private final Map<Parameter, CheckBox> checkBoxMap = new HashMap<>();

    @FXML
    public void initialize() {
        // Initialize the API controller
        weatherBox = new ApiController();

        // Create checkboxes for each parameter
        for (Parameter param : params) {
            CheckBox cb = new CheckBox(param.getDisplayName());
            checkboxContainer.getChildren().add(cb);
            checkBoxMap.put(param, cb);
        }

        // Set default date to today if not already set
        if (date.getValue() == null) {
            date.setValue(LocalDate.now());
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
    protected void onButtonClick() {

        try {
            weatherBox.fetchWeather(placeText.getText(), params);

        } catch (Exception e) {
            welcomeText.setText("Error fetching weather data: " + e.getMessage());
            e.printStackTrace();
        }
    }
}