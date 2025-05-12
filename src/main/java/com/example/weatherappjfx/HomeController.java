package com.example.weatherappjfx;

import com.example.weatherappjfx.api.ApiController;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeController {
    private Map<String, String> weatherData;
    private ApiController weatherBox;
    public VBox checkboxContainer;
    List<Parameter> params = List.of(
            new Parameter("temperature_2m", "Temperatura"),
            new Parameter("precipitation", "Opad atmosferyczny"),
            new Parameter("rain", "Deszcz"),
            new Parameter("wind_speed_10m", "Prędkość wiatru"),
            new Parameter("soil_temperature_0cm", "Temperatura Gleby"),
            new Parameter("surface_pressure", "Ciśnienie przy powierzchni")

            //,rain,showers,snowfall,relative_humidity_2m,apparent_temperature,is_day,weather_code,cloud_cover,pressure_msl,surface_pressure,wind_speed_10m,wind_direction_10m,wind_gusts_10m
    );

    @FXML
    private Label welcomeText;
    @FXML
    private TextField placeText;

    @FXML
    private DatePicker date;
    @FXML
    private TextArea dataText;

    private final Map<Parameter, CheckBox> checkBoxMap = new HashMap<>();

    @FXML
    public void initialize() {
        // Initialize the API controller
        weatherBox = new ApiController();
        weatherData = new HashMap<>();

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
    protected void onButtonClick() {

        try {
            JsonObject json = new Gson().fromJson(weatherBox.fetchWeather(placeText.getText(), getSelectedApiKeys()), JsonObject.class);
            JsonObject data = json.getAsJsonObject("current");
            dataText.setEditable(false);
            int skipCount =0;
            for(String key: data.keySet()) {
                if (skipCount < 2) {
                    skipCount++;
                    continue;
                }
                JsonElement element = data.get(key);
                weatherData.put(key, element.getAsString());
                System.out.println(weatherData.get(key));
                dataText.appendText(key + ": "+ weatherData.get(key) + "\n" + element.getAsString());
            }



        } catch (Exception e) {
            System.out.println(e);
            e.printStackTrace();
        }
    }
}