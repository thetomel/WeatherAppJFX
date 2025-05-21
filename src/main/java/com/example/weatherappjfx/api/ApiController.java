package com.example.weatherappjfx.api;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
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
            HttpClient geoClient = HttpClient.newHttpClient();
            String geoUrl = "https://geocoding-api.open-meteo.com/v1/search?name=" + name + "&count=1&language=pl&format=json";
            HttpRequest geoRequest = HttpRequest
                    .newBuilder()
                    .uri(URI.create(geoUrl))
                    .GET()
                    .build();

            HttpResponse<String> repsonse = geoClient.newBuilder()
                    .build()
                    .send(geoRequest, HttpResponse.BodyHandlers.ofString());

            double[] geo = extractCoordinates(String.valueOf(repsonse.body()));
            return geo;
        }
        catch (Exception e){
            throw new RuntimeException(e);
        }
    }
    public String fetchWeather(String place, List<String> params, dateType type, String startDate, String endDate) {
        double[] geoPosition = findPlace(place);
        try {
            StringBuilder urlFetch = new StringBuilder(url + "forecast?latitude=" + geoPosition[0] + "&longitude=" + geoPosition[1]);

            if (type == dateType.CURRENT) {
                urlFetch.append("&current=");
            } else {
                urlFetch.append("&hourly=");
            }

            for (String item : params) {
                urlFetch.append(',').append(item);
            }

            if (type == dateType.FORECAST || type == dateType.ARCHIVAL) {
                    urlFetch.append("&start_date=").append(startDate).append("&end_date=").append(endDate);
            }

            HttpClient weatherClient = HttpClient.newHttpClient();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(urlFetch.toString()))
                    .GET()
                    .build();

            HttpResponse<String> response = weatherClient.send(request, HttpResponse.BodyHandlers.ofString());
            return response.body();
        } catch (Exception e) {
            throw new RuntimeException("Error while fetching weather data", e);
        }
    }

}
