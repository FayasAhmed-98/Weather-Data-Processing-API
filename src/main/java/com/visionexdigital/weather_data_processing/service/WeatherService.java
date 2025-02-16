package com.visionexdigital.weather_data_processing.service;

import com.visionexdigital.weather_data_processing.exception.WeatherErrorHandler;
import com.visionexdigital.weather_data_processing.exception.WeatherServiceException;
import com.visionexdigital.weather_data_processing.model.WeatherSummary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Service
public class WeatherService {

    private static final Logger logger = LoggerFactory.getLogger(WeatherService.class);

    private final WebClient webClient;

    @Value("${weather.api.key}")
    private String apiKey;

    @Value("${weather.forecast.days}")
    private int forecastDay;
    public WeatherService(WebClient.Builder webClientBuilder, @Value("${weather.api.url}") String apiUrl) {
        this.webClient = webClientBuilder.baseUrl(apiUrl).build();
    }

    /**
     * Asynchronously fetches the weather summary for a given city.
     * Caches the result to avoid redundant API calls.
     */
    @Async
    @Cacheable(value = "weather", key = "#city", unless = "#result == null")
    public CompletableFuture<WeatherSummary> getWeatherSummary(String city) {
        LocalDate today = LocalDate.now();
        LocalDate lastWeek = today.minusDays(forecastDay);

        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("key", apiKey)
                        .queryParam("q", city)
                        .queryParam("dt", lastWeek.toString())  // Fetch past 7 days
                        .queryParam("end_dt", today.toString())
                        .build())
                .retrieve()
                .bodyToMono(Map.class)
                .doOnNext(data -> logger.info("Received API Response: {}", data))
                .flatMap(data -> {
                    try {
                        return Mono.just(extractAndCalculateWeatherData(data)); // Process and calculate weather data
                    } catch (RuntimeException e) {
                        logger.error("Error processing weather data: {}", e.getMessage());
                        return Mono.error(new WeatherServiceException("Error processing weather data", e));
                    }
                })
                .onErrorResume(WebClientResponseException.class, e -> {
                    String errorMessage = WeatherErrorHandler.handleWebClientError(e, city);
                    logger.error(errorMessage);
                    return Mono.error(new WeatherServiceException(errorMessage, e));
                })
                .toFuture();
    }

    /**
     * Extracts and processes weather data from the API response.
     * Calculates average temperature and determines the hottest and coldest days.
     */
    private WeatherSummary extractAndCalculateWeatherData(Map<String, Object> data) {
        if (data == null || !data.containsKey("forecast")) {
            logger.error("Missing 'forecast' key in API response: {}", data);
            throw new WeatherServiceException("Weather data not found for the given city.");
        }

        List<Map<String, Object>> forecastDays = (List<Map<String, Object>>)
                ((Map<String, Object>) data.get("forecast")).get("forecastday");

        if (forecastDays == null || forecastDays.isEmpty()) {
            logger.error("No past weather data found in API response.");
            throw new WeatherServiceException("No past weather data available for this city.");
        }

        LocalDate today = LocalDate.now();
        LocalDate lastWeek = today.minusDays(forecastDay);

        double totalTemp = 0;
        Map<LocalDate, Double> dailyTemperatures = new HashMap<>();

        // Process the weather forecast for the past 7 days
        for (Map<String, Object> day : forecastDays) {
            LocalDate date = LocalDate.parse(day.get("date").toString());

            // Only consider data for the past 7 days
            if (!date.isBefore(lastWeek) && !date.isAfter(today)) {
                double avgTemp = Optional.ofNullable(((Map<String, Object>) day.get("day")).get("avgtemp_c"))
                        .map(temp -> ((Number) temp).doubleValue())
                        .orElse(0.0);

                totalTemp += avgTemp;
                dailyTemperatures.put(date, avgTemp);
            }
        }

        if (dailyTemperatures.isEmpty()) {
            logger.error("No valid temperature data available.");
            throw new WeatherServiceException("Weather data missing for past 7 days.");
        }

        LocalDate hottestDay = Collections.max(dailyTemperatures.entrySet(), Map.Entry.comparingByValue()).getKey();
        LocalDate coldestDay = Collections.min(dailyTemperatures.entrySet(), Map.Entry.comparingByValue()).getKey();

        return new WeatherSummary(
                (String) ((Map<String, Object>) data.get("location")).get("name"),
                Math.round(totalTemp / dailyTemperatures.size() * 10.0) / 10.0, // Round to 1 decimal place
                hottestDay.toString(),
                coldestDay.toString()
        );
    }
}
