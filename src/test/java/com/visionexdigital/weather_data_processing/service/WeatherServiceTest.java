package com.visionexdigital.weather_data_processing.service;

import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import com.visionexdigital.weather_data_processing.model.WeatherSummary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import java.util.concurrent.CompletableFuture;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for WeatherService to verify the behavior of weather-related API endpoints.
 */
@WireMockTest(httpPort = 8081)
@SpringBootTest(properties = "weather.api.url=http://localhost:8081/v1/history.json")
public class WeatherServiceTest {

    @Autowired
    private WeatherService weatherService;

    // The mock response for successful weather data retrieval
    private static final String MOCK_WEATHER_RESPONSE = """
        {
          "location": { "name": "TestCity" },
          "forecast": {
            "forecastday": [
              { "date": "2025-02-09", "day": { "avgtemp_c": 15.0 } },
              { "date": "2025-02-10", "day": { "avgtemp_c": 16.0 } },
              { "date": "2025-02-11", "day": { "avgtemp_c": 18.0 } },
              { "date": "2025-02-12", "day": { "avgtemp_c": 20.0 } },
              { "date": "2025-02-13", "day": { "avgtemp_c": 22.0 } },
              { "date": "2025-02-14", "day": { "avgtemp_c": 19.0 } },
              { "date": "2025-02-15", "day": { "avgtemp_c": 21.0 } }
            ]
          }
        }
    """;

    /**
     * Setup mock weather API response for the tests.
     */
    @BeforeEach
    void setUp() {
        stubFor(get(urlPathEqualTo("/v1/history.json"))
                .withQueryParam("q", equalTo("TestCity"))
                .withQueryParam("key", equalTo("dummy-key"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(MOCK_WEATHER_RESPONSE)));
    }

    /**
     * Test the successful weather summary retrieval.
     */
    @Test
    public void testGetWeatherSummary_Success() {
        WeatherSummary result = weatherService.getWeatherSummary("TestCity").join();

        assertNotNull(result);
        assertEquals("TestCity", result.getCity());
        assertEquals(18.71, result.getAverageTemperature(), 0.1);
    }

    /**
     * Test the case when the city is not found and an error is returned.
     */
    @Test
    void testGetWeatherSummary_whenCityNotFound_thenThrowsRuntimeException() {
        stubFor(get(urlPathEqualTo("/v1/history.json"))
                .withQueryParam("q", equalTo("InvalidCity"))
                .withQueryParam("key", equalTo("dummy-key"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.BAD_REQUEST.value())
                        .withBody("""
                            {"error": {"code": 1006, "message": "No matching location found."}}
                        """)));

        CompletableFuture<WeatherSummary> futureSummary = weatherService.getWeatherSummary("InvalidCity");

        assertThrows(RuntimeException.class, futureSummary::join);
    }

    /**
     * Test the case when the API returns an empty response.
     */
    @Test
    void testGetWeatherSummary_whenEmptyResponse_thenThrowsRuntimeException() {
        stubFor(get(urlPathEqualTo("/v1/history.json"))
                .withQueryParam("q", equalTo("NoDataCity"))
                .withQueryParam("key", equalTo("dummy-key"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withBody("{}"))); // Empty JSON response

        CompletableFuture<WeatherSummary> futureSummary = weatherService.getWeatherSummary("NoDataCity");

        assertThrows(RuntimeException.class, futureSummary::join);
    }

    /**
     * Test the case when the API returns a malformed response.
     */
    @Test
    void testGetWeatherSummary_whenMalformedResponse_thenThrowsRuntimeException() {
        stubFor(get(urlPathEqualTo("/v1/history.json"))
                .withQueryParam("q", equalTo("TestCity"))
                .withQueryParam("key", equalTo("dummy-key"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withBody("INVALID_JSON"))); // Invalid JSON

        CompletableFuture<WeatherSummary> futureSummary = weatherService.getWeatherSummary("TestCity");

        assertThrows(RuntimeException.class, futureSummary::join);
    }
}
