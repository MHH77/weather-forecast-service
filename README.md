# Weather Forecast Service

A simple RESTful service to fetch current weather information for a given city using the OpenWeatherMap API.

[![Java](https://img.shields.io/badge/Java-17+-blue?style=flat-square&logo=openjdk)](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.5-brightgreen?style=flat-square&logo=spring)](https://spring.io/projects/spring-boot)
[![Maven](https://img.shields.io/badge/Maven-3.8+-blue?style=flat-square&logo=apache-maven)](https://maven.apache.org/)

## Description

This project provides a simple Spring Boot-based API that accepts a city name as input, queries the OpenWeatherMap API for the current weather data (like temperature, description, humidity, and wind speed), and returns it in JSON format.

## Features

*   **REST API:** A single `GET /api/weather` endpoint to retrieve data.
*   **OpenWeatherMap Integration:** Connects to the external API for live weather data.
*   **Data Transfer Objects (DTOs):** Uses DTOs (`WeatherResponseDTO`, `ErrorResponseDTO`) to shape API responses.
*   **Robust Error Handling:** Implements custom exceptions and a Global Exception Handler (`@ControllerAdvice`) to return meaningful error responses with appropriate HTTP status codes.
*   **Caching:** Server-side caching implemented using Spring Cache and Caffeine to reduce external API calls and improve response times.
*   **API Documentation:** Interactive API documentation generated using Springdoc OpenAPI (Swagger UI).
*   **Testing:** Includes Unit Tests for the Service layer (with Mockito) and Integration Tests for the Controller layer (with MockMvc).
*   **Clean Architecture:** Follows good design principles with separated layers (Controller, Service).

## Technology Stack

*   **Language:** Java 17+
*   **Framework:** Spring Boot 3.2.x
*   **Build & Dependency Management:** Apache Maven
*   **Core Spring Libraries:** Spring Web, Spring Cache
*   **JSON Processing:** Jackson Databind
*   **Utility:** Lombok (to reduce boilerplate code)
*   **External Service:** OpenWeatherMap API
*   **Caching:** Caffeine
*   **API Documentation:** Springdoc OpenAPI (Swagger UI)
*   **Testing:** JUnit 5, Mockito, Spring Test

## Prerequisites

Before you begin, ensure you have the following installed:
1.  **JDK 17 or higher:** [Oracle JDK](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html) or [OpenJDK](https://adoptium.net/)
2.  **Apache Maven:** [Installation Guide](https://maven.apache.org/install.html)
3.  **OpenWeatherMap API Key:**
    *   Go to the [OpenWeatherMap](https://openweathermap.org/) website.
    *   Sign up (or log in).
    *   Obtain a free API key from the API keys section in your user profile. *Note: It might take a few minutes to a couple of hours for the key to become active.*

## Setup and Run

1.  **Clone the Repository:**
    ```bash
    git clone <YOUR_REPOSITORY_URL>
    cd weather-forecast-service
    ```

2.  **Configure API Key:**
    *   Open the `src/main/resources/application.properties` file.
    *   Locate the `openweathermap.api.key` property.
    *   Replace `YOUR_API_KEY_HERE` with the actual API key you obtained from OpenWeatherMap.
    ```properties
    # OpenWeatherMap Configuration
    openweathermap.api.key=YOUR_ACTUAL_API_KEY_HERE
    openweathermap.api.url=https://api.openweathermap.org/data/2.5/weather
    openweathermap.api.units=metric    # Cache Configuration (Using Caffeine)
    spring.cache.cache-names=weatherCache
    spring.cache.caffeine.spec=maximumSize=500,expireAfterWrite=10m
    ```
    *   Save the file.

3.  **Build the Project (Optional):**
    ```bash
    mvn clean package
    ```
    This command downloads dependencies, compiles the project, and creates an executable JAR file in the `target` directory.

4.  **Run the Application:**
    You can run the application using the Maven plugin:
    ```bash
    mvn spring-boot:run
    ```
    Or, if you built the project, run the JAR file:
    ```bash
    java -jar target/weather-forecast-service-0.0.1-SNAPSHOT.jar
    ```
    (The JAR filename might vary slightly)

    The service will be available at `http://localhost:8080`.

## How to Use

### Main Endpoint

*   **Method:** `GET`
*   **URL:** `/api/weather`
*   **Query Parameter:**
    *   `city` (string, **required**): The name of the city for which you want the weather forecast.

### Example Request

```bash
curl -X GET "http://localhost:8080/api/weather?city=London"
```

### API Endpoint

Or simply open in your browser:  
[http://localhost:8080/api/weather?city=Tehran](http://localhost:8080/api/weather?city=Tehran)

## ✅ Example Success Response (Code 200 OK)

```json
{
  "cityName": "London",
  "temperature": 15.5,
  "description": "broken clouds",
  "humidity": 75,
  "windSpeed": 4.1
}
```

---

## ❌ Example Error Response (Code 404 Not Found)

```json
{
  "timestamp": "2025-04-29T12:30:00.123456",
  "status": 404,
  "error": "Not Found",
  "message": "Weather data not found for city: InvalidCityName",
  "path": "/api/weather"
}
```

---

## 📘 API Documentation (Swagger UI)

Interactive API documentation is available via **Swagger UI**.  
Once the application is running, navigate to:  
[http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

Through this interface, you can:
- View endpoint details
- See parameters and responses
- Execute requests directly

The OpenAPI definition in JSON format is also available at:  
[http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

---

## 🧪 Running Tests

To run the unit and integration tests, use the following Maven command:

```bash
mvn test
```

---

## ⚙️ Key Implementation Details

### 🧠 Caching

- Utilizes **Spring Cache Abstraction** with **Caffeine**.
- Results from `getWeatherData` method in `WeatherServiceImpl` are cached using `@Cacheable`.
- Cache name: `weatherCache`
- Configuration (in `application.properties`):
    - `expireAfterWrite`
    - `maximumSize`
- Cache key is the lowercase version of the city name (e.g., `London` and `london` map to the same key).

---

### 🛠️ Error Handling

- Custom exceptions:
    - `CityNotFoundException`
    - `ExternalApiException`
    - `InvalidInputException`
- Global exception management via `@ControllerAdvice` in `GlobalExceptionHandler`.
- Specific `@ExceptionHandler` methods return a standardized `ErrorResponseDTO` with correct HTTP status codes.
