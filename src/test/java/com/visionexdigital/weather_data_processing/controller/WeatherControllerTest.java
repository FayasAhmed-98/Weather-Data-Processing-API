package com.visionexdigital.weather_data_processing.controller;

import com.visionexdigital.weather_data_processing.model.WeatherSummary;
import com.visionexdigital.weather_data_processing.service.WeatherService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.ResponseEntity;

import java.util.concurrent.CompletableFuture;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the WeatherController class, verifying behavior for successful and error scenarios.
 */
class WeatherControllerTest {

    @Mock
    private WeatherService weatherService;

    @InjectMocks
    private WeatherController weatherController;

    private static final String CITY = "London";  // Constant for the city used in tests

    /**
     * Set up the mock service before each test.
     */
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);  // Initialize mocks
    }

    /**
     * Test case for successfully retrieving weather information.
     * Verifies that the controller correctly processes a successful response from the service.
     */
    @Test
    void testGetWeather_whenServiceReturnsWeatherSummary_thenReturnsOkResponse() throws Exception {
        // Prepare mock weather summary
        WeatherSummary mockWeatherSummary = new WeatherSummary("London", 20.5, "2025-02-01", "2025-02-10");

        // Simulate the service call returning a completed future
        when(weatherService.getWeatherSummary(CITY)).thenReturn(CompletableFuture.completedFuture(mockWeatherSummary));

        // Call the controller method
        CompletableFuture<ResponseEntity<WeatherSummary>> response = weatherController.getWeather(CITY);

        // Block until the response is returned
        ResponseEntity<WeatherSummary> entity = response.get();  // .get() is needed for CompletableFuture

        // Validate response status and body
        assertEquals(200, entity.getStatusCodeValue());  // Check for OK status
        assertNotNull(entity.getBody());  // Ensure the response body is not null
        assertEquals("London", entity.getBody().getCity());  // Validate the city name
        assertEquals(20.5, entity.getBody().getAverageTemperature());  // Validate the temperature
    }

    /**
     * Test case for when an error occurs in the service layer.
     * Verifies that the controller handles service failures and returns the correct error response.
     */
    @Test
    void testGetWeather_whenServiceThrowsError_thenReturnsBadRequestResponse() throws Exception {
        // Simulate the service throwing an error by returning a failed future
        when(weatherService.getWeatherSummary(CITY)).thenReturn(CompletableFuture.failedFuture(new RuntimeException("API Error")));

        // Call the controller method
        CompletableFuture<ResponseEntity<WeatherSummary>> response = weatherController.getWeather(CITY);

        // Block until the response is returned
        ResponseEntity<WeatherSummary> entity = response.get();

        // Validate the error response (should return BAD_REQUEST)
        assertEquals(400, entity.getStatusCodeValue());  // Check for BAD_REQUEST status
        assertNull(entity.getBody());  // Ensure the response body is null
    }

}
