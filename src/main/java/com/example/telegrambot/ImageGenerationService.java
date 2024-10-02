package com.example.telegrambot;

import okhttp3.*;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class ImageGenerationService {
    private final String apiKey;
    private final OkHttpClient client;

    public ImageGenerationService(String apiKey) {
        this.apiKey = apiKey;
        this.client = new OkHttpClient();
    }

    public String generateImage(String prompt) throws IOException, JSONException {
        // Создаем JSON-объект для запроса
        JSONObject json = new JSONObject();
        json.put("prompt", prompt);
        json.put("n", 1);
        json.put("size", "1024x1024");

        // Формируем запрос
        RequestBody body = RequestBody.create(json.toString(), MediaType.get("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url("https://api.openai.com/v1/images/generations")
                .post(body)
                .addHeader("Authorization", "Bearer " + apiKey)
                .build();

        // Выполняем запрос
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
            // Извлекаем URL изображения из ответа
            String responseBody = response.body().string();
            return parseImageUrl(responseBody);
        }
    }

    private String parseImageUrl(String jsonResponse) throws JSONException {
        JSONObject jsonObject = new JSONObject(jsonResponse);
        return jsonObject.getJSONArray("data").getJSONObject(0).getString("url");
    }
}
