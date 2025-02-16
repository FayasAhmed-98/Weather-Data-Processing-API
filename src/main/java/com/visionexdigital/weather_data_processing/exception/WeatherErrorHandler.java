package com.visionexdigital.weather_data_processing.exception;

import com.visionexdigital.weather_data_processing.model.WeatherSummary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.WebClientResponseException;

public class WeatherErrorHandler {

    private static final Logger logger = LoggerFactory.getLogger(WeatherErrorHandler.class);

    /**
     * Handles exceptions occurring during weather data retrieval.
     *
     * @param city The city for which weather data was requested.
     * @param ex   The exception thrown during processing.
     * @return A ResponseEntity containing an appropriate error message.
     */
    public static ResponseEntity<WeatherSummary> handleWeatherServiceException(String city, Throwable ex) {
        String errorMessage = (ex.getCause() != null) ? ex.getCause().getMessage() : "Unknown error";

        // If the error is related to an invalid city (e.g., user input issue), return 400 with message
        if (errorMessage.toLowerCase().contains("invalid city") || errorMessage.toLowerCase().contains("city not found")) {
            logger.error("City '{}' not found: {}", city, errorMessage);
            return ResponseEntity.badRequest().body(new WeatherSummary(city, -1.0, null, null,
                    "City not found. Please check the name and try again."));
        }

        // Handle network or timeout errors (e.g., WebClient specific errors)
        if (ex instanceof WebClientResponseException) {
            return ResponseEntity.status(500).body(new WeatherSummary(city, -1.0, null, null,
                    handleWebClientError((WebClientResponseException) ex, city)));
        }

        // For unexpected errors, return 400 without a body, or handle accordingly
        logger.error("Unexpected error fetching weather data for '{}': {}", city, errorMessage);
        return ResponseEntity.badRequest().body(null);  // Return null body
    }


    /**
     * Handles WebClient-specific errors and returns a relevant error message.
     */
    public static String handleWebClientError(WebClientResponseException e, String city) {
        if (e.getStatusCode().is4xxClientError()) {
            return "Invalid city name: " + city;
        } else {
            return "Weather API is currently unavailable. Please try again later. Error: " + e.getResponseBodyAsString();
        }
    }
}
