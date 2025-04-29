package org.mhh.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mhh.dto.WeatherResponseDTO;
import org.mhh.exception.CityNotFoundException;
import org.mhh.exception.ExternalApiException;
import org.mhh.service.WeatherService;
import org.springframework.beans.factory.annotation.Autowired;import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest; // ۱. فقط لایه وب رو تست می‌کنه
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@WebMvcTest(controllers = WeatherController.class)
class WeatherControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WeatherService weatherService;

    @Autowired
    private ObjectMapper objectMapper;

    void getWeatherForecast_Success() throws Exception {

        String city = "London";
        WeatherResponseDTO mockResponse = new WeatherResponseDTO(city, 15.0, "clear sky", 80, 5.0);        // به سرویس ماک شده می‌گیم وقتی با شهر لندن صدا زده شد، این پاسخ رو برگردون
        given(weatherService.getWeatherData(city)).willReturn(mockResponse);


        mockMvc.perform(get("/api/weather")
                        .param("city", city)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.cityName", is(city)))
                .andExpect(jsonPath("$.temperature", is(15.0)))
                .andExpect(jsonPath("$.description", is("clear sky")));

        then(weatherService).should(times(1)).getWeatherData(city);
    }

    @Test
    void getWeatherForecast_CityNotFound() throws Exception {
        String city = "InvalidCity";
        given(weatherService.getWeatherData(city)).willThrow(new CityNotFoundException(city));

        mockMvc.perform(get("/api/weather")
                        .param("city", city)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("Not Found")))
                .andExpect(jsonPath("$.message", containsString(city)));

        then(weatherService).should(times(1)).getWeatherData(city);
    }

    @Test
    void getWeatherForecast_MissingCityParam() throws Exception {

        mockMvc.perform(get("/api/weather")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.message", containsString("Required parameter 'city' is missing"))); // پیام خطای MissingServletRequestParameterException

        then(weatherService).should(never()).getWeatherData(anyString());
    }

    @Test
    void getWeatherForecast_ExternalApiError() throws Exception {
        String city = "ProblemCity";
        given(weatherService.getWeatherData(city)).willThrow(new ExternalApiException("Service unavailable"));

        mockMvc.perform(get("/api/weather")
                        .param("city", city)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isServiceUnavailable())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is(503)))
                .andExpect(jsonPath("$.error", is("Service Unavailable")))
                .andExpect(jsonPath("$.message", containsString("Error communicating with external weather service")));

        then(weatherService).should(times(1)).getWeatherData(city);
    }

}
