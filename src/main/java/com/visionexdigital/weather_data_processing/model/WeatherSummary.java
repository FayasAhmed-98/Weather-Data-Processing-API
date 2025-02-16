package com.visionexdigital.weather_data_processing.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class WeatherSummary {
    private String city;
    private double averageTemperature;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String hottestDay;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String coldestDay;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String errorMessage;
    public WeatherSummary(String city, double averageTemperature, String hottestDay, String coldestDay) {
        this.city = city;
        this.averageTemperature = averageTemperature;
        this.hottestDay = hottestDay;
        this.coldestDay = coldestDay;
    }

    // Constructor for error response
    public WeatherSummary(String city, double averageTemperature, String hottestDay, String coldestDay, String errorMessage) {
        this.city = city;
        this.averageTemperature = averageTemperature;
        this.hottestDay = hottestDay;
        this.coldestDay = coldestDay;
        this.errorMessage = errorMessage;
    }
    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public double getAverageTemperature() {
        return averageTemperature;
    }

    public void setAverageTemperature(double averageTemperature) {
        this.averageTemperature = averageTemperature;
    }

    public String getHottestDay() {
        return hottestDay;
    }

    public void setHottestDay(String hottestDay) {
        this.hottestDay = hottestDay;
    }

    public String getColdestDay() {
        return coldestDay;
    }

    public void setColdestDay(String coldestDay) {
        this.coldestDay = coldestDay;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}