package com.example.telegrambot;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ChatGPTService {
    private final String apiKey;

    public ChatGPTService(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getChatGPTResponse(String userInput) {
        try {
            // Создаем URL для API OpenAI
            URL url = new URL("https://api.openai.com/v1/chat/completions");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            // Устанавливаем метод запроса и заголовки
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + apiKey);
            conn.setDoOutput(true);

            // Создаем JSON-объект с параметрами для запроса
            JSONObject jsonRequest = new JSONObject();
            jsonRequest.put("model", "gpt-3.5-turbo"); // Укажите модель
            JSONArray messages = new JSONArray();
            messages.put(new JSONObject().put("role", "user").put("content", userInput));
            jsonRequest.put("messages", messages);

            // Отправляем запрос
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonRequest.toString().getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            // Получаем ответ от API
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }

            // Обрабатываем ответ
            return extractChatGPTMessage(response.toString());
        } catch (Exception e) {
            e.printStackTrace();
            return "Ошибка при получении ответа от ChatGPT.";
        }
    }

    private String extractChatGPTMessage(String jsonResponse) {
        try {
            JSONObject jsonObject = new JSONObject(jsonResponse);
            JSONArray choices = jsonObject.getJSONArray("choices");
            String content = choices.getJSONObject(0).getJSONObject("message").getString("content");
            return content.trim(); // Удаляем лишние пробелы
        } catch (JSONException e) {
            e.printStackTrace();
            return "Ошибка обработки ответа ChatGPT.";
        }
    }
}
