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
            new Parameter( "precipitation", "Opad atmosferyczny"),
            new Parameter( "rain", "Deszcz"),
            new Parameter( "wind_speed_10m", "Prędkość wiatru"),
            new Parameter("soil_temperature_0cm", "Temperatura Gleby"),
            new Parameter("surface_pressure", "Ciśnienie przy powierzchni")

            //,rain,showers,snowfall,relative_humidity_2m,apparent_temperature,is_day,weather_code,cloud_cover,pressure_msl,surface_pressure,wind_speed_10m,wind_direction_10m,wind_gusts_10m
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
            weatherBox.fetchWeather(placeText.getText(), getSelectedApiKeys());

        } catch (Exception e) {
            System.out.println(e);
            e.printStackTrace();
        }
    }
}