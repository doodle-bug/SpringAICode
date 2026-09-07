package com.fr3nzy.SpringAICode.config;


import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
//import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.ai.vectorstore.redis.RedisVectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPooled;
import redis.clients.jedis.RedisClient;
//import org.springframework.jdbc.core.JdbcTemplate;

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
}
