package com.Artiom.ArtifexAI.Common.Database;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;

@Configuration
@ConfigurationProperties(prefix = "primary.mongodb")
public class PrimaryMongoConfig {
    @Value("${primary.mongodb.uri}")
    private String uri;
    @Value("${primary.mongodb.database}")
    private String database;

    private MongoClient mongoClient;

    @Bean(name = "mongoTemplate")
    public MongoTemplate mongoTemplate() {
        mongoClient = MongoClients.create(uri);
        return new MongoTemplate(mongoClient, database);
    }

    @PreDestroy
    public void cleanup() {
        if (mongoClient != null) {
            mongoClient.close();
        }
    }
}
