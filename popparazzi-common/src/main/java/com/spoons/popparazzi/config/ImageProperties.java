package com.spoons.popparazzi.config;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.image")
public class ImageProperties {

    /**
     * 게시글(모임후기 등) 썸네일이 없을 때 내려줄 기본 이미지 URL
     * yml: app.image.default-board-thumbnail
     */
    private String defaultBoardThumbnail;
}