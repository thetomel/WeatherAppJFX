package com.example.weatherappjfx.api;

import com.example.weatherappjfx.Parameter;
import javafx.concurrent.Task;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ApiController {
    String url = "https://api.open-meteo.com/v1/";
    public double[] extractCoordinates(String jsonResponse) {
        try {
            ObjectMapper mapper = new ObjectMapper();

            JsonNode rootNode = mapper.readTree(jsonResponse);
            JsonNode resultsNode = rootNode.path("results");

            if (resultsNode.isMissingNode() || resultsNode.isEmpty()) {
                System.err.println("No results found in JSON response");
                return new double[] {0.0, 0.0};
            }

            JsonNode firstResult = resultsNode.get(0);

            double latitude = firstResult.path("latitude").asDouble(0.0);
            double longitude = firstResult.path("longitude").asDouble(0.0);
            System.out.println("Extracted coordinates: " + latitude + ", " + longitude);
            return new double[] {latitude, longitude};

        } catch (Exception e) {
            System.err.println("Error parsing JSON: " + e.getMessage());
            if (jsonResponse != null) {
                int previewLength = Math.min(jsonResponse.length(), 100);
                System.err.println("JSON response preview: " + jsonResponse.substring(0, previewLength) +
                        (jsonResponse.length() > previewLength ? "..." : ""));
            }

            return new double[] {0.0, 0.0};
        }
    }

    public double[] findPlace(String name){
        try {
            System.out.println(name);
            HttpClient geoClient = HttpClient.newHttpClient();
            String geoUrl = "https://geocoding-api.open-meteo.com/v1/search?name=" + name + "&count=1&language=pl&format=json";
            HttpRequest geoRequest = HttpRequest.newBuilder().uri(URI.create(geoUrl)).GET().build();

            HttpResponse<String> repsonse = geoClient.newBuilder()
                    .build()
                    .send(geoRequest, HttpResponse.BodyHandlers.ofString());

            double[] geo = extractCoordinates(String.valueOf(repsonse.body()));
            System.out.println(geoUrl);
            return geo;
        }
        catch (Exception e){
            throw new RuntimeException(e);
        }
    }
    public void fetchWeather(String place, List<Parameter> params) {
        double[] geoPosition = findPlace(place);
        try {
            Task<String> task = new Task<>() {
                @Override
                protected String call() throws Exception {
                    String urlFetch = url + " forecast?latitude=" + geoPosition[0] + "&longitude=" + geoPosition[1] + "&hourly=temperature_2m";

                    HttpClient client = HttpClient.newHttpClient();

                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create(urlFetch))
                            .GET()
                            .build();

                    client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                            .thenApply(HttpResponse::body)
                            .thenAccept(response -> {

                            })
                            .join();
                    return null;
                }
            };
        }
        catch(Exception e) {
            throw new RuntimeException("Error while fetching weather data");

        }
    }
}
