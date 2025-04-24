package org.mhh.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mhh.dto.WeatherResponseDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class WeatherServiceImpl implements WeatherService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
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
    public WeatherResponseDTO getWeatherData(String city) {
        String url = UriComponentsBuilder.fromHttpUrl(apiUrl)
                .queryParam("q", city)
                .queryParam("appid", apiKey)
                .queryParam("units", apiUnits)
                .toUriString();

        System.out.println("Requesting URL: " + url);

        try {
            String jsonResponse = restTemplate.getForObject(url, String.class);
            System.out.println("Received JSON response: " + jsonResponse);
            return mapJsonToDto(jsonResponse, city);

        } catch (HttpClientErrorException e) {
            System.err.printf("HTTP Error calling OpenWeatherMap API for city %s: %s - %s%n", city, e.getStatusCode(), e.getResponseBodyAsString());
            // TODO: Throw specific exceptions
            return null;
        } catch (RestClientException | JsonProcessingException e) {
            System.err.printf("Error processing weather data for city %s: %s%n", city, e.getMessage());
            // TODO: Throw specific exceptions
            return null;
        }
    }

    private WeatherResponseDTO mapJsonToDto(String jsonResponse, String requestedCity) throws JsonProcessingException {
        JsonNode rootNode = objectMapper.readTree(jsonResponse);
        int responseCode = rootNode.path("cod").asInt();
        if (responseCode != 200) {
            String errorMessage = rootNode.path("message").asText("Unknown error from OpenWeatherMap");
            System.err.println("OpenWeatherMap API returned error code " + responseCode + ": " + errorMessage);
            // TODO: Throw specific exception like CityNotFoundException
            return null;
        }

        String cityName = rootNode.path("name").asText(requestedCity);
        Double temperature = rootNode.path("main").path("temp").asDouble();
        String description = rootNode.path("weather").get(0).path("description").asText("N/A");
        Integer humidity = rootNode.path("main").path("humidity").asInt();
        Double windSpeed = rootNode.path("wind").path("speed").asDouble();
        return new WeatherResponseDTO(cityName, temperature, description, humidity, windSpeed);
    }
}