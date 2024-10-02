package com.example.telegrambot;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigLoader {
    private String botToken;
    private String botUsername;
    private String openAiApiKey;

    public ConfigLoader() {
        loadProperties();
    }

    private void loadProperties() {
        Properties properties = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                System.out.println("Sorry, unable to find config.properties");
                return;
            }
            properties.load(input);
            botToken = properties.getProperty("telegram.bot.token");
            botUsername = properties.getProperty("telegram.bot.username");
            openAiApiKey = properties.getProperty("openai.api.key");

            // Логирование для отладки
            System.out.println("Bot Username: " + botUsername);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public String getBotToken() {
        return botToken;
    }

    public String getBotUsername() {
        return botUsername;
    }

    public String getOpenAiApiKey() {
        return openAiApiKey;
    }
}
