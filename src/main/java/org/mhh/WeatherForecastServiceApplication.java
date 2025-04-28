package org.mhh;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class WeatherForecastServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(WeatherForecastServiceApplication.class, args);
        System.out.println("Weather Forecast Service is running!");
    }

}