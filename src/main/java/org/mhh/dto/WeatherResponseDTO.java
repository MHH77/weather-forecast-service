package org.mhh.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data // ۱. شامل @Getter, @Setter, @ToString, @EqualsAndHashCode, @RequiredArgsConstructor
@NoArgsConstructor
@AllArgsConstructor
public class WeatherResponseDTO {

    private String cityName;
    private Double temperature;
    private String description;
    private Integer humidity;
    private Double windSpeed;
}