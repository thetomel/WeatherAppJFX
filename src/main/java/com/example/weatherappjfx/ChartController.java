package com.example.weatherappjfx;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChartController implements Initializable {

    @FXML
    private LineChart<Number, Number> weatherChart;

    @FXML
    private NumberAxis xAxis;

    @FXML
    private NumberAxis yAxis;

    @FXML
    private VBox parameterCheckboxContainer;

    @FXML
    private ScrollPane checkboxScrollPane;

    private String rawData;
    private Map<String, XYChart.Series<Number, Number>> seriesMap = new HashMap<>();
    private Map<String, CheckBox> checkBoxMap = new HashMap<>();
    private List<WeatherDataPoint> dataPoints = new ArrayList<>();

    private final String[] CHART_COLORS = {
            "#FF6B6B", "#4ECDC4", "#45B7D1", "#96CEB4", "#FFEAA7",
            "#DDA0DD", "#98D8C8", "#F7DC6F", "#BB8FCE", "#85C1E9",
            "#F8C471", "#82E0AA", "#F1948A", "#85C1E9", "#D7DBDD"
    };

    private int colorIndex = 0;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupChart();
        setupCheckboxContainer();
    }

    private void setupChart() {
        xAxis.setLabel("Czas (godziny od początku)");
        yAxis.setLabel("Wartość");
        weatherChart.setTitle("Wykres Danych Pogodowych");
        weatherChart.setCreateSymbols(false);
        weatherChart.setLegendVisible(true);
        xAxis.setAutoRanging(true);
        yAxis.setAutoRanging(true);
        weatherChart.setAnimated(false);
    }

    private void setupCheckboxContainer() {
        parameterCheckboxContainer.setSpacing(8);
        checkboxScrollPane.setFitToWidth(true);
        checkboxScrollPane.setPrefHeight(200);
    }

    public void setChartData(String data) {
        this.rawData = data;
        parseWeatherData(data);
        createParameterCheckboxes();
        updateChart();
    }

    private void parseWeatherData(String data) {
        dataPoints.clear();
            parseTimeSeriesData(data);
    }

    private void parseTimeSeriesData(String data) {
        String[] sections = data.split("Czas: ");

        for (int i = 1; i < sections.length; i++) {
            String section = sections[i];
            WeatherDataPoint point = new WeatherDataPoint();

            String[] lines = section.split("\n");
            if (lines.length > 0) {
                String timeStr = lines[0].trim();
                point.timeIndex = i - 1;

                try {
                    if (timeStr.contains("T")) {
                        point.timestamp = LocalDateTime.parse(timeStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                    } else if (timeStr.length() >= 10) {
                        point.timestamp = LocalDateTime.parse(timeStr + "T00:00:00");
                    }
                } catch (DateTimeParseException e) {
                    point.timestamp = LocalDateTime.now().plusHours(i - 1);
                }

                for (int j = 1; j < lines.length; j++) {
                    String line = lines[j].trim();
                    if (line.startsWith("- ") && line.contains(":")) {
                        String cleanLine = line.substring(2); // Usuń "- "
                        String[] parts = cleanLine.split(":");
                        if (parts.length >= 2) {
                            String paramName = parts[0].trim();
                            String valueStr = parts[1].trim();
                            Pattern numberPattern = Pattern.compile("(-?\\d+(?:\\.\\d+)?)");
                            Matcher matcher = numberPattern.matcher(valueStr);
                            if (matcher.find()) {
                                try {
                                    double value = Double.parseDouble(matcher.group(1));
                                    point.parameters.put(paramName, value);
                                } catch (NumberFormatException e) {
                                    System.err.println("Nie można sparsować wartości: " + valueStr);
                                }
                            }
                        }
                    }
                }

                if (!point.parameters.isEmpty()) {
                    dataPoints.add(point);
                }
            }
        }
    }

    private void createParameterCheckboxes() {
        parameterCheckboxContainer.getChildren().clear();
        checkBoxMap.clear();

        Set<String> allParameters = new HashSet<>();
        for (WeatherDataPoint point : dataPoints) {
            allParameters.addAll(point.parameters.keySet());
        }

        for (String parameter : allParameters) {
            CheckBox checkBox = new CheckBox(parameter);
            checkBox.setSelected(true);
            checkBox.setOnAction(e -> updateChart());

            checkBoxMap.put(parameter, checkBox);
            parameterCheckboxContainer.getChildren().add(checkBox);
        }
    }

    private void updateChart() {
        weatherChart.getData().clear();
        seriesMap.clear();
        colorIndex = 0;

        Set<String> selectedParameters = new HashSet<>();
        for (Map.Entry<String, CheckBox> entry : checkBoxMap.entrySet()) {
            if (entry.getValue().isSelected()) {
                selectedParameters.add(entry.getKey());
            }
        }

        for (String parameter : selectedParameters) {
            XYChart.Series<Number, Number> series = new XYChart.Series<>();
            series.setName(parameter);

            for (WeatherDataPoint point : dataPoints) {
                if (point.parameters.containsKey(parameter)) {
                    Double value = point.parameters.get(parameter);
                    if (value != null) {
                        series.getData().add(new XYChart.Data<>(point.timeIndex, value));
                    }
                }
            }

            if (!series.getData().isEmpty()) {
                seriesMap.put(parameter, series);
                weatherChart.getData().add(series);

                setSeriesColor(series, CHART_COLORS[colorIndex % CHART_COLORS.length]);
                colorIndex++;
            }
        }
        updateChartTitle();
    }

    private void setSeriesColor(XYChart.Series<Number, Number> series, String color) {
        Platform.runLater(() -> {
            if (weatherChart.lookupAll(".chart-series-line").size() > 0) {
                String cssColor = color.replace("#", "");
                series.getNode().setStyle("-fx-stroke: " + color + "; -fx-stroke-width: 2px;");
            }
        });
    }

    private void updateChartTitle() {
        long selectedCount = checkBoxMap.values().stream()
                .mapToLong(cb -> cb.isSelected() ? 1 : 0)
                .sum();

        if (selectedCount == 0) {
            weatherChart.setTitle("Wykres Danych Pogodowych - Brak wybranych parametrów");
        } else if (selectedCount == 1) {
            String selectedParam = checkBoxMap.entrySet().stream()
                    .filter(entry -> entry.getValue().isSelected())
                    .map(Map.Entry::getKey)
                    .findFirst()
                    .orElse("");
            weatherChart.setTitle("Wykres Danych Pogodowych - " + selectedParam);
        } else {
            weatherChart.setTitle("Wykres Danych Pogodowych - " + selectedCount + " parametrów");
        }
    }

    private static class WeatherDataPoint {
        LocalDateTime timestamp;
        int timeIndex;
        Map<String, Double> parameters = new HashMap<>();
    }
}