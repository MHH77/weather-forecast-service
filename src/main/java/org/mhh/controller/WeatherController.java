package org.mhh.controller;

import org.mhh.dto.WeatherResponseDTO;
import org.mhh.service.WeatherService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/weather")
public class WeatherController {

    private static final Logger log = LoggerFactory.getLogger(WeatherController.class); // لاگر برای کنترلر
    private final WeatherService weatherService;

    public WeatherController(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    @GetMapping
    // نوع بازگشتی رو به DTO مشخص می‌کنیم
    public ResponseEntity<WeatherResponseDTO> getWeatherForecast(@RequestParam String city) {
        // ۱. نیازی به چک کردن city خالی نیست، InvalidInputException توسط سرویس throw می‌شه
        // ۲. نیازی به چک کردن null بودن نتیجه نیست، Exception ها توسط GlobalExceptionHandler گرفته می‌شن

        log.info("Received request for city: {}", city);        // فقط سرویس رو صدا می‌زنیم
        WeatherResponseDTO weatherData = weatherService.getWeatherData(city);        // اگه به اینجا برسیم یعنی همه چی موفقیت آمیز بوده
        return ResponseEntity.ok(weatherData);    }
}