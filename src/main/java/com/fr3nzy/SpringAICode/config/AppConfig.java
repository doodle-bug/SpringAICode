package com.fr3nzy.SpringAICode.config;


import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.image.Image;
import org.springframework.ai.image.ImageGeneration;
import org.springframework.ai.image.ImageModel;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.image.ImageResponse;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
//import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.ai.vectorstore.redis.RedisVectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.util.UriComponentsBuilder;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPooled;
import redis.clients.jedis.RedisClient;
//import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

@Configuration
public class AppConfig {

//    @Bean
//    public VectorStore vectorStore(JdbcTemplate jdbcTemplate, EmbeddingModel embeddingModel){
//        return PgVectorStore.builder(jdbcTemplate, embeddingModel).build();
//    }

    @Bean
    public RedisClient jedisClient() {
        return RedisClient.builder().hostAndPort("localhost", 6379).build();
    }

    @Bean
    public VectorStore vectorStore(RedisClient jedisClient, EmbeddingModel embeddingModel) {
        return RedisVectorStore.builder(jedisClient, embeddingModel)
                .indexName("product-index")
                .prefix("product")
                .initializeSchema(true)
                .build();
    }

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