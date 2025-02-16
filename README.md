# Weather Data Processing Application

## Overview

This Spring Boot application fetches, processes, and analyzes weather data from an external API. It provides a RESTful endpoint to retrieve weather summaries for a given city while implementing advanced Spring features such as asynchronous execution and caching.

## Features

- Fetches weather data from an external API (weatherapi).
- Computes:
  - Average temperature for the last 7 days.(configurable)
  - Hottest and coldest days.
- Caches results to minimize API calls (30-minute timeout).
- Supports asynchronous data fetching for improved performance.
- Includes unit tests using JUnit and Mockito.
- Handles errors for invalid city names and API failures.

## API Endpoint

### Get Weather Summary

```
GET /weather?city={city_name}
```

#### Request Example:

```
GET /weather?city=London
```

#### Response Example:

```json
{
  "city": "London",
  "averageTemperature": 15.5,
  "hottestDay": "2024-11-20",
  "coldestDay": "2024-11-18"
}
```

## Technologies Used

- **Spring Boot 3+**
- **Spring Web** (REST API development)
- **Spring Cache** 
- **Spring WebFlux (WebClient)** (External API integration)
- **JUnit & Mockito** (Unit testing)
- **Lombok** (for reducing boilerplate code)

## Setup and Running the Application

### Prerequisites

- Java 17+
- Maven 3+

### Steps to Run

1. Clone the repository:
   ```sh
   git clone https://github.com/FayasAhmed-98/Weather-Data-Processing-API
   cd Weather-Data-Processing-API
   ```
2. Configure the API key for weatherapi in `application.properties`:
   ```properties
   weather.api.key=YOUR_API_KEY
   ```
3. Build and run the application:
   ```sh
   mvn clean install
   mvn spring-boot:run
   ```

## Running Tests

Run unit tests using:

```sh
mvn test
```

## Future Improvements

- Add support for multiple external weather APIs.
- Implement a database layer for historical weather data storage.
- Enhance logging and monitoring.

