package org.mhh.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mhh.dto.WeatherResponseDTO;
import org.mhh.exception.CityNotFoundException;
import org.mhh.exception.ExternalApiException;
import org.mhh.exception.InvalidInputException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class WeatherServiceImpl implements WeatherService {

    private static final Logger log = LoggerFactory.getLogger(WeatherServiceImpl.class);
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${openweathermap.api.key}")
    private String apiKey;
    @Value("${openweathermap.api.url}")
    private String apiUrl;
    @Value("${openweathermap.api.units}")
    private String apiUnits;

    public WeatherServiceImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    @Cacheable(value = "weatherCache", key = "#city.toLowerCase()")
    public WeatherResponseDTO getWeatherData(String city) {
        log.info(">>> Fetching weather data for city: {} (Cache MISS or expired)", city);

        if (city == null || city.trim().isEmpty()) {
            throw new InvalidInputException("City name cannot be empty.");
        }

        String url = buildUrl(city);
        log.debug("Requesting URL: {}", url);

        try {
            String jsonResponse = restTemplate.getForObject(url, String.class);
            log.debug("Received JSON response: {}", jsonResponse);
            return mapJsonToDto(jsonResponse, city);

        } catch (HttpClientErrorException e) {
            log.error("HTTP Error calling OpenWeatherMap API for city {}: {} - {}", city, e.getStatusCode(), e.getResponseBodyAsString(), e);
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new CityNotFoundException(city, e);
            } else {
                throw new ExternalApiException("Received HTTP error " + e.getStatusCode() + " from external service.", e);
            }
        } catch (RestClientException e) {
            log.error("Network or communication error calling OpenWeatherMap API for city {}: {}", city, e.getMessage(), e);
            throw new ExternalApiException("Could not communicate with the external weather service.", e);
        } catch (JsonProcessingException e) {
            log.error("Error parsing JSON response for city {}: {}", city, e.getMessage(), e);
            throw new ExternalApiException("Failed to parse response from external service.", e);
        }
    }

    private String buildUrl(String city) {
        return UriComponentsBuilder.fromHttpUrl(apiUrl)
                .queryParam("q", city)
                .queryParam("appid", apiKey)
                .queryParam("units", apiUnits)
                .toUriString();
    }

    private WeatherResponseDTO mapJsonToDto(String jsonResponse, String requestedCity) throws JsonProcessingException, CityNotFoundException {
        JsonNode rootNode = objectMapper.readTree(jsonResponse);
        int responseCode = rootNode.path("cod").asInt();
        if (responseCode != 200) {
            String errorMessage = rootNode.path("message").asText("Unknown error from OpenWeatherMap");
            log.warn("OpenWeatherMap API returned non-200 code {} for city {}: {}", responseCode, requestedCity, errorMessage);
            if (responseCode == 404) {
                throw new CityNotFoundException(requestedCity);
            } else {
                throw new ExternalApiException("External service returned error code " + responseCode + ": " + errorMessage);
            }
        }
        String cityName = rootNode.path("name").asText(requestedCity);
        Double temperature = rootNode.path("main").path("temp").asDouble();
        String description = rootNode.path("weather").get(0).path("description").asText("N/A");
        Integer humidity = rootNode.path("main").path("humidity").asInt();
        Double windSpeed = rootNode.path("wind").path("speed").asDouble();

        return new WeatherResponseDTO(cityName, temperature, description, humidity, windSpeed);
    }
}
