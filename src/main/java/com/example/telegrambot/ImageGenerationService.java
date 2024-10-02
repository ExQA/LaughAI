package com.example.telegrambot;

import okhttp3.*;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.concurrent.TimeUnit;

public class ImageGenerationService {
    private static final String API_URL = "https://api.openai.com/v1/images/generations";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private final OkHttpClient client;
    private final String openAiApiKey;

    public ImageGenerationService(String openAiApiKey) {
        this.openAiApiKey = openAiApiKey;

        // Настройка клиента с таймаутами
        this.client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    public String generateImage(String prompt) throws SocketTimeoutException {
        int attempts = 0;

        while (attempts < 3) {
            try {
                // Создаем JSON-объект для запроса
                JSONObject json = new JSONObject();
                json.put("prompt", prompt);
                json.put("n", 1); // Количество изображений для генерации
                json.put("size", "1024x1024"); // Размер изображения

                RequestBody body = RequestBody.create(json.toString(), JSON);
                Request request = new Request.Builder()
                        .url(API_URL)
                        .addHeader("Authorization", "Bearer " + openAiApiKey)
                        .post(body)
                        .build();

                // Выполняем запрос и получаем ответ
                Response response = client.newCall(request).execute();
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected code " + response);
                }

                // Обрабатываем ответ
                String responseBody = response.body().string();
                JSONObject responseJson = new JSONObject(responseBody);
                return responseJson.getJSONArray("data").getJSONObject(0).getString("url");
            } catch (SocketTimeoutException e) {
                attempts++;
                if (attempts >= 3) {
                    throw e; // Пробрасываем исключение, если превышено количество попыток
                }
                // Ждем 2 секунды перед повторной попыткой
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt(); // Восстанавливаем прерывание
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace(); // Логируем ошибку
                return "Произошла ошибка при генерации изображения.";
            }
        }
        return null; // Возвращаем null, если не удалось получить изображение
    }
}