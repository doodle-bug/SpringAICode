package com.fr3nzy.SpringAICode.config;

import org.springframework.ai.image.Image;
import org.springframework.ai.image.ImageGeneration;
import org.springframework.ai.image.ImageModel;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.image.ImageResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@Configuration
public class ImageConfig {

    @Bean
    @Primary
    public ImageModel freeImageModel() {
        return new ImageModel() {
            @Override
            public ImageResponse call(ImagePrompt request) {
                // Extract prompt text
                String promptText = request.getInstructions().get(0).getText();

                // Free public image generation service (Pollinations.ai)
                String generatedUrl = UriComponentsBuilder
                        .fromUriString("https://image.pollinations.ai/prompt/")
                        .pathSegment(promptText)
                        .queryParam("nologo", "true")
                        .toUriString();

                Image image = new Image(generatedUrl, null);
                ImageGeneration generation = new ImageGeneration(image);

                return new ImageResponse(List.of(generation));
            }
        };
    }
}