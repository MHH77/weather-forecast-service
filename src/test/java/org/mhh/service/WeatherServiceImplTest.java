package org.mhh.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mhh.dto.WeatherResponseDTO;
import org.mhh.exception.CityNotFoundException;
import org.mhh.exception.ExternalApiException;
import org.mhh.exception.InvalidInputException;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class WeatherServiceImplTest {

    @Mock
    private RestTemplate restTemplate;

    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private WeatherServiceImpl weatherService;

    @BeforeEach
    void setUp() {
        weatherService = new WeatherServiceImpl(restTemplate);
        // TODO: Set private fields apiKey, apiUrl, apiUnits if needed for complex tests
        org.springframework.test.util.ReflectionTestUtils.setField(weatherService, "apiKey", "test-key");
        org.springframework.test.util.ReflectionTestUtils.setField(weatherService, "apiUrl", "http://test.com");
        org.springframework.test.util.ReflectionTestUtils.setField(weatherService, "apiUnits", "metric");
        org.springframework.test.util.ReflectionTestUtils.setField(weatherService, "objectMapper", objectMapper);

    }

    @Test
    void getWeatherData_Success() throws JsonProcessingException {
        String city = "London";
        String fakeJsonResponse = """
                {                    "coord": {"lon": -0.1257, "lat": 51.5085},
                    "weather": [{"id": 803, "main": "Clouds", "description": "broken clouds", "icon": "04n"}],
                    "base": "stations",
                    "main": {"temp": 15.5, "feels_like": 14.8, "temp_min": 14.0, "temp_max": 16.5, "pressure": 1012, "humidity": 75},
                    "visibility": 10000,
                    "wind": {"speed": 4.1, "deg": 240},
                    "clouds": {"all": 75},                    "dt": 1678886400,
                    "sys": {"type": 1, "id": 1414, "country": "GB", "sunrise": 1678857600, "sunset": 1678900800},
                    "timezone": 0,
                    "id": 2643743,
                    "name": "London",
                    "cod": 200
                }
                """;

        when(restTemplate.getForObject(anyString(), eq(String.class)))
                .thenReturn(fakeJsonResponse);
        WeatherResponseDTO result = weatherService.getWeatherData(city);
        assertNotNull(result);
        assertEquals("London", result.getCityName());
        assertEquals(15.5, result.getTemperature());
        assertEquals("broken clouds", result.getDescription());
        assertEquals(75, result.getHumidity());
        assertEquals(4.1, result.getWindSpeed());

        verify(restTemplate, times(1)).getForObject(anyString(), eq(String.class));
    }

    @Test
    void getWeatherData_CityNotFound_Api404() {
        String city = "InvalidCity";
        when(restTemplate.getForObject(anyString(), eq(String.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND, "Not Found"));

        CityNotFoundException exception = assertThrows(CityNotFoundException.class, () -> {
            weatherService.getWeatherData(city);
        });

        assertTrue(exception.getMessage().contains(city));
        verify(restTemplate, times(1)).getForObject(anyString(), eq(String.class));
    }

    @Test
    void getWeatherData_CityNotFound_Json404() {
        String city = "AnotherInvalidCity";
        String fakeJsonResponse = """
                {"cod": "404", "message": "city not found"}
                """;
        when(restTemplate.getForObject(anyString(), eq(String.class)))
                .thenReturn(fakeJsonResponse);

        assertThrows(CityNotFoundException.class, () -> {
            weatherService.getWeatherData(city);
        });
        verify(restTemplate, times(1)).getForObject(anyString(), eq(String.class));
    }

    @Test
    void getWeatherData_Unauthorized_Api401() {
        String city = "Tehran";
        when(restTemplate.getForObject(anyString(), eq(String.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED, "Invalid API key"));

        ExternalApiException exception = assertThrows(ExternalApiException.class, () -> {
            weatherService.getWeatherData(city);
        });

        assertFalse(exception.getMessage().contains("Invalid API key"));
        verify(restTemplate, times(1)).getForObject(anyString(), eq(String.class));
    }

    @Test
    void getWeatherData_NetworkError() {
        String city = "Paris";
        when(restTemplate.getForObject(anyString(), eq(String.class)))
                .thenThrow(new RestClientException("Could not connect"));

        ExternalApiException exception = assertThrows(ExternalApiException.class, () -> {
            weatherService.getWeatherData(city);
        });

        assertTrue(exception.getMessage().contains("Could not communicate"));
        verify(restTemplate, times(1)).getForObject(anyString(), eq(String.class));
    }

    @Test
    void getWeatherData_EmptyCity() {
        String city = "";

        InvalidInputException exception = assertThrows(InvalidInputException.class, () -> {
            weatherService.getWeatherData(city);
        });

        assertTrue(exception.getMessage().contains("cannot be empty"));

        verify(restTemplate, never()).getForObject(anyString(), any());
    }

    @Test
    void getWeatherData_NullCity() {
        String city = null;

        assertThrows(InvalidInputException.class, () -> {
            weatherService.getWeatherData(city);
        });
        verify(restTemplate, never()).getForObject(anyString(), any());
    }

}