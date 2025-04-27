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
    public WeatherResponseDTO getWeatherData(String city) {
        // ۱. اعتبار سنجی ورودی در همین لایه سرویس
        if (city == null || city.trim().isEmpty()) {
            throw new InvalidInputException("City name cannot be empty.");
        }

        String url = buildUrl(city);
        log.info("Requesting URL: {}", url);

        try {
            String jsonResponse = restTemplate.getForObject(url, String.class);
            log.debug("Received JSON response: {}", jsonResponse);
            // ۲. mapJsonToDto حالا می‌تونه CityNotFoundException هم throw کنه
            return mapJsonToDto(jsonResponse, city);

        } catch (HttpClientErrorException e) {
            // ۳. مدیریت دقیق‌تر خطاهای HTTP            log.error("HTTP Error calling OpenWeatherMap API for city {}: {} - {}", city, e.getStatusCode(), e.getResponseBodyAsString(), e);
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) { // کد 404 از OpenWeather
                throw new CityNotFoundException(city, e);
            } else if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) { // کد 401
                throw new ExternalApiException("Invalid API key or unauthorized access.", e);
            } else {
                // بقیه خطاهای 4xx یا 5xx از OpenWeather
                throw new ExternalApiException("Received HTTP error " + e.getStatusCode() + " from external service.", e);
            }        } catch (RestClientException e) {
            // ۴. خطاهای کلی ارتباط (شبکه، DNS و ...)
            log.error("Network or communication error calling OpenWeatherMap API for city {}: {}", city, e.getMessage(), e);
            throw new ExternalApiException("Could not communicate with the external weather service.", e);
        } catch (JsonProcessingException e) {
            // ۵. خطای پردازش JSON (بعیده ولی ممکنه)
            log.error("Error parsing JSON response for city {}: {}", city, e.getMessage(), e);
            throw new ExternalApiException("Failed to parse response from external service.", e);
        }        // catch (Exception e) { // یه catch عمومی هم بد نیست برای خطاهای پیش‌بینی نشده
        //     log.error("Unexpected error processing weather data for city {}: {}", city, e.getMessage(), e);
        //     throw new ExternalApiException("An unexpected error occurred.", e);
        // }
    }

    private String buildUrl(String city) {
        // متد کمکی برای ساخت URL
        return UriComponentsBuilder.fromHttpUrl(apiUrl)
                .queryParam("q", city)
                .queryParam("appid", apiKey)
                .queryParam("units", apiUnits)
                .toUriString();
    }

    // ۶. آپدیت mapJsonToDto برای throw کردن CityNotFoundException
    private WeatherResponseDTO mapJsonToDto(String jsonResponse, String requestedCity) throws JsonProcessingException, CityNotFoundException {
        JsonNode rootNode = objectMapper.readTree(jsonResponse);

        // OpenWeather کد وضعیت رو داخل JSON هم می‌فرسته
        // "cod": 200 یعنی موفقیت آمیز
        // "cod": "404", message: "city not found"
        int responseCode = rootNode.path("cod").asInt(); // یا .asText() اگه ممکنه رشته باشه
        if (responseCode != 200) {
            String errorMessage = rootNode.path("message").asText("Unknown error from OpenWeatherMap");
            log.warn("OpenWeatherMap API returned non-200 code {} for city {}: {}", responseCode, requestedCity, errorMessage);
            if (responseCode == 404) { // به طور صریح 404 رو به CityNotFoundException تبدیل می‌کنیم
                throw new CityNotFoundException(requestedCity);
            } else {
                // بقیه کدهای خطای OpenWeather
                throw new ExternalApiException("External service returned error code " + responseCode + ": " + errorMessage);
            }
        }

        // استخراج اطلاعات (بقیه کد مثل قبل)
        String cityName = rootNode.path("name").asText(requestedCity);
        Double temperature = rootNode.path("main").path("temp").asDouble();
        String description = rootNode.path("weather").get(0).path("description").asText("N/A");
        Integer humidity = rootNode.path("main").path("humidity").asInt();
        Double windSpeed = rootNode.path("wind").path("speed").asDouble();

        return new WeatherResponseDTO(cityName, temperature, description, humidity, windSpeed);
    }
}
