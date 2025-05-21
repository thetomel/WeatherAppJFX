package com.example.weatherappjfx;

import com.example.weatherappjfx.api.dateType;

public class Parameter {
    private final String apiKey;
    private final String displayName;
    private final dateType dateType;

    public Parameter(String apikey, String displayName, dateType dateType) {
        this.displayName = displayName;
        this.apiKey = apikey;
        this.dateType = dateType;
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getDisplayName() {
        return displayName;
    }

    public dateType getDateType() { return dateType; }
}
