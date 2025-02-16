package com.visionexdigital.weather_data_processing.controller;

import com.visionexdigital.weather_data_processing.exception.WeatherErrorHandler;
import com.visionexdigital.weather_data_processing.model.WeatherSummary;
import com.visionexdigital.weather_data_processing.service.WeatherService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

/**
 * Controller for handling weather-related requests.
 */
@RestController
public class WeatherController {

    private static final Logger logger = LoggerFactory.getLogger(WeatherController.class);
    private final WeatherService weatherService;

    /**
     * Constructor-based dependency injection for WeatherService.
     *
     * @param weatherService The service handling weather data fetching and processing.
     */
    public WeatherController(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    /**
     * Retrieves weather summary for a given city.
     *
     * @param city The city name provided as a query parameter.
     * @return containing a ResponseEntity with WeatherSummary data.
     */
    @GetMapping("/weather")
    public CompletableFuture<ResponseEntity<WeatherSummary>> getWeather(@RequestParam String city) {
        return weatherService.getWeatherSummary(city)
                .thenApply(weather -> {
                    logger.info("Weather data successfully retrieved for city: {}", city);
                    return ResponseEntity.ok(weather);
                })
                .exceptionally(ex -> {
                    // Handle errors by passing the error message
                    return WeatherErrorHandler.handleWeatherServiceException(city, ex);
                });
    }
}
