package com.quynhlm.dev.be.Configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
public class ConfigEnv {
    @Value("${email.password}")
    private String emailPassword;

    @Value("${aws.access.key.id}")
    private String awsAccessKeyId;

    @Value("${aws.secret.access.key}")
    private String awsSecretAccessKey;

    @Value("${db.name}")
    private String dbName;

    @Value("${db.pass}")
    private String dbPass;

    @PostConstruct
    public void init() {
        System.out.println("Email Password: " + emailPassword);
        System.out.println("AWS Access Key ID: " + awsAccessKeyId);
        System.out.println("AWS Secret Access Key: " + awsSecretAccessKey);
        System.out.println("DB_NAME: " + dbName);
        System.out.println("DB_PASS: " + dbPass);
    }
}
