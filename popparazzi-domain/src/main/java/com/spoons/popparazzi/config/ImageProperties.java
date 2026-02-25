package com.spoons.popparazzi.config;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Component
@Validated
@ConfigurationProperties(prefix = "app.image")
public class ImageProperties {

    @NotBlank(message = "app.image.default-board-thumbnail 설정이 필요합니다.")
    private String defaultBoardThumbnail;
}