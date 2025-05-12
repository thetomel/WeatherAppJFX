package com.example.weatherappjfx;

public class Parameter {
    private final String apiKey;
    private final String displayName;
    //private String value;

    public Parameter(String apikey, String displayName) {
        this.displayName = displayName;
        this.apiKey = apikey;
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getDisplayName() {
        return displayName;
    }
}
