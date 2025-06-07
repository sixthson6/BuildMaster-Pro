package com.tech.auditlog;


import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;

@Configuration
public class MongoConfig extends AbstractMongoClientConfiguration {

    @Override
    protected @NotNull String getDatabaseName() {
        return "tech_audit_db";
    }

    @Override
    protected boolean autoIndexCreation() {
        return true;
    }
}
