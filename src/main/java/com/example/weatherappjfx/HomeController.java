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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeController {
    private Map<String, String> weatherData;
    private ApiController weatherBox;
    public VBox checkboxContainer;
    List<Parameter> params = List.of(
            //CURRENT
            new Parameter("temperature_2m", "Temperatura", dateType.CURRENT),
            new Parameter("apparent_temperature", "Temperatura odczuwalna", dateType.CURRENT),
            new Parameter("precipitation", "Opad atmosferyczny", dateType.CURRENT),
            new Parameter("rain", "Deszcz", dateType.CURRENT),
            new Parameter("wind_speed_10m", "Prędkość wiatru", dateType.CURRENT),
            new Parameter("relative_humidity_2m", "Wilgotność względna", dateType.CURRENT),
            new Parameter( "surface_pressure", "Cisnienie", dateType.CURRENT),
            //FORECAST
            new Parameter("temperature_2m", "Temperatura", dateType.FORECAST),
            new Parameter("soil_temperature_0cm", "Temperatura Gleby", dateType.FORECAST),
            new Parameter("snowfall", "Opady śniegu", dateType.FORECAST),
            new Parameter("snow_depth", "Głębokość śniegu", dateType.FORECAST),
            new Parameter("apparent_temperature", "Temperatura odczuwalna", dateType.FORECAST),
            new Parameter("precipitation", "Opad atmosferyczny", dateType.FORECAST),
            new Parameter("rain", "Deszcz", dateType.FORECAST),
            new Parameter("wind_speed_10m", "Prędkość wiatru", dateType.FORECAST),
            new Parameter("relative_humidity_2m", "Wilgotność względna", dateType.FORECAST),
            new Parameter( "surface_pressure", "Ciśnienie", dateType.FORECAST),
            new Parameter( "cloud_cover", "Zachmurzenie", dateType.FORECAST),
            // ARCHIVAL
            new Parameter("temperature_2m", "Temperatura", dateType.ARCHIVAL),
            new Parameter("soil_temperature_0cm", "Temperatura Gleby", dateType.ARCHIVAL),
            new Parameter("snowfall", "Opady śniegu", dateType.ARCHIVAL),
            new Parameter("snow_depth", "Głębokość śniegu", dateType.ARCHIVAL),
            new Parameter("apparent_temperature", "Temperatura odczuwalna", dateType.CURRENT),
            new Parameter("precipitation", "Opad atmosferyczny", dateType.CURRENT),
            new Parameter("rain", "Deszcz", dateType.CURRENT),
            new Parameter("wind_speed_10m", "Prędkość wiatru", dateType.CURRENT),
            new Parameter("relative_humidity_2m", "Wilgotność względna", dateType.CURRENT),
            new Parameter( "surface_pressure", "Ciśnienie", dateType.CURRENT),
            new Parameter( "cloud_cover", "Zachmurzenie", dateType.CURRENT)
    );

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
        updateCheckboxesForDateType(currentDateType);
        switchVisibility(datePickerContainer, false);
        startDate.setValue(LocalDate.now());
        endDate.setValue(LocalDate.now());
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
            updateCheckboxesForDateType(currentDateType);
        });
    }

    private void updateCheckboxesForDateType(dateType selectedDateType) {
        checkboxContainer.getChildren().clear();
        checkBoxMap.clear();

        for (Parameter param : params) {
            if (param.getDateType() == selectedDateType) {
                CheckBox cb = new CheckBox(param.getDisplayName());
                checkboxContainer.getChildren().add(cb);
                checkBoxMap.put(param, cb);
            }
        }
    }

    void switchVisibility(HBox hbox, boolean visible) {
        hbox.setVisible(visible);
        hbox.setManaged(visible);
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
            if(dateChoiceBox.getSelectionModel().getSelectedItem().equals("Aktualna")) {
                startDate.setValue(LocalDate.now());
            }

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            JsonObject json = new Gson().fromJson(weatherBox.fetchWeather(placeText.getText(),
                    getSelectedApiKeys(), currentDateType,
                    formatter.format(startDate.getValue()), formatter.format(endDate.getValue())), JsonObject.class);
            weatherData.clear();
            dataText.clear();

            switch (currentDateType) {
                case CURRENT: {
                    JsonObject data = json.getAsJsonObject("current");
                    processCurrentWeather(data);
                    break;
                }
                case ARCHIVAL: {
                    processTimeSeriesWeather(json, "Dane archiwalne");
                    break;
                }
                case FORECAST: {
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
        return key; // Zwróć oryginalną nazwę, jeśli nie znajdziesz dopasowania
    }

    private String formatWeatherDataEntry(String key, String value, String unit) {
        return formatParameterName(key) + ": " + value + " " + unit;
    }

    private String getUnitForParameter(String parameterName) {
        switch (parameterName) {
            case "temperature_2m": return "°C";
            case "apparent_temperature": return "°C";
            case "surface_pressure": return "hPa";
            case "wind_speed_10m": return "km/h";
            case "rain": return "mm";
            case "precipitation": return "mm";
            case "soil_temperature_0cm": return "°C";
            case "relative_humidity_2m": return "%";
            case "cloud_cover": return "%";
            case "snowfall": return "cm";
            default: return "";
        }
    }
}