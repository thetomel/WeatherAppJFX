package com.example.weatherappjfx;

import com.example.weatherappjfx.api.ApiController;
import com.example.weatherappjfx.api.dateType;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

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
    private DatePicker startDate;
    @FXML
    private DatePicker endDate;
    @FXML
    private TextArea dataText;
    @FXML
    private ChoiceBox<String> dateChoiceBox;
    @FXML
    private HBox datePickerContainer;

    private dateType currentDateType = dateType.CURRENT;


    private final Map<Parameter, CheckBox> checkBoxMap = new HashMap<>();

    @FXML
    public void initialize() {
        weatherBox = new ApiController();
        weatherData = new HashMap<>();
        dataText.setEditable(false);

        for (Parameter param : params) {
            CheckBox cb = new CheckBox(param.getDisplayName());
            checkboxContainer.getChildren().add(cb);
            checkBoxMap.put(param, cb);
        }

        switchVisibility(datePickerContainer, false);

        dateChoiceBox.getItems().addAll("Archiwalna", "Aktualna", "Prognoza");
        dateChoiceBox.setValue("Aktualna");
        dateChoiceBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, value) -> {
            switchVisibility(datePickerContainer, true);
            switch (value) {
                case "Archiwalna":
                    currentDateType = dateType.ARCHIVAL;
                    break;
                case "Aktualna":
                    currentDateType = dateType.CURRENT;
                    switchVisibility(datePickerContainer, false);
                    break;
                case "Prognoza":
                    currentDateType = dateType.FORECAST;
                    break;
            }
        });
    }

    void switchVisibility(HBox vbox, boolean visible) {
        vbox.setVisible(visible);
        vbox.setManaged(visible);
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
            JsonObject json = new Gson().fromJson(weatherBox.fetchWeather(placeText.getText(), getSelectedApiKeys(), currentDateType), JsonObject.class);
            weatherData.clear();
            dataText.clear();

            switch (currentDateType) {
                case dateType.CURRENT: {
                    JsonObject data = json.getAsJsonObject("current");
                    processCurrentWeather(data);
                    break;
                }
                case dateType.ARCHIVAL: {
                    processTimeSeriesWeather(json, "Dane archiwalne");
                    break;
                }
                case dateType.FORECAST: {
                    processTimeSeriesWeather(json, "Prognoza pogody");
                    break;
                }
            }
        } catch (Exception e) {
            System.err.println("Wystąpił błąd podczas przetwarzania danych pogodowych: " + e.getMessage());
            e.printStackTrace();
            dataText.setText("Błąd przetwarzania danych: " + e.getMessage());
        }
    }

    private void processCurrentWeather(JsonObject data) {
        if (data == null) {
            dataText.setText("Brak danych dla aktualnej pogody");
            return;
        }

        StringBuilder output = new StringBuilder("AKTUALNA POGODA:\n");
        int skipCount = 0;
        for (String key : data.keySet()) {
            if (skipCount < 2) {
                skipCount++;
                continue;
            }
            JsonElement element = data.get(key);
            String value = element.isJsonPrimitive() ? element.getAsString() : element.toString();
            weatherData.put(key, value);
            String unit = getUnitForParameter(key);
            output.append(formatWeatherDataEntry(key, value, unit)).append("\n");
        }
        dataText.setText(output.toString());
    }

    private void processTimeSeriesWeather(JsonObject json, String title) {
        if (json == null || !json.has("hourly")) {
            dataText.setText("Brak danych dla " + title.toLowerCase());
            return;
        }

        JsonObject hourly = json.getAsJsonObject("hourly");
        JsonObject hourlyUnits = json.getAsJsonObject("hourly_units");
        JsonArray timeArray = hourly.getAsJsonArray("time");
        StringBuilder output = new StringBuilder(title.toUpperCase() + ":\n\n");

        for (int i = 0; i < timeArray.size(); i++) {
            String time = timeArray.get(i).getAsString();
            output.append("Czas: ").append(time).append("\n");

            for (String key : hourly.keySet()) {
                if (!key.equals("time")) {
                    JsonArray valueArray = hourly.getAsJsonArray(key);
                    if (i < valueArray.size()) {
                        String value = valueArray.get(i).getAsString();
                        String unit = hourlyUnits.has(key) ? " " + hourlyUnits.get(key).getAsString() : "";
                        String mapKey = key + "_" + i;
                        weatherData.put(mapKey, value);
                        output.append("  - ").append(formatParameterName(key)).append(": ")
                                .append(value).append(unit).append("\n");
                    }
                }
            }
            output.append("\n");
        }

        dataText.setText(output.toString());
    }

    private String formatParameterName(String key) {
        for (Parameter param : params) {
            if (param.getApiKey().equals(key)) {
                return param.getDisplayName();
            }
        }
        return " - ";
    }

    private String formatWeatherDataEntry(String key, String value, String unit) {
        return formatParameterName(key) + ": " + value + " " + unit;
    }

    private String getUnitForParameter(String parameterName) {
        switch (parameterName) {
            case "temperature_2m": return "°C";
            case "surface_pressure": return "hPa";
            case "wind_speed_10m": return "km/h";
            case "rain":return "mm";
            case "soil_temperature_0cm": return "°C";
            default: return "";
        }
    }
}