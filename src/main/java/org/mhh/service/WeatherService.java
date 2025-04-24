package org.mhh.service;

import org.mhh.dto.WeatherResponseDTO;

public interface WeatherService {
    WeatherResponseDTO getWeatherData(String city);

}
