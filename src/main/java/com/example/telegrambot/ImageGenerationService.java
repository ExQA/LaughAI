package com.example.telegrambot;

import okhttp3.*;
import org.json.JSONException;
import org.json.JSONObject;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
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

    public void generateImage(String prompt, long chatId, TelegramLongPollingBot bot) throws SocketTimeoutException {
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
                String imageUrl = responseJson.getJSONArray("data").getJSONObject(0).getString("url");

                // Скачиваем изображение
                String localFilePath = downloadImage(imageUrl);
                sendImageToTelegram(chatId, localFilePath, bot);
                return; // Выходим после успешной отправки

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
                return; // Обрабатываем ошибку, возвращаем управление
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private String downloadImage(String imageUrl) throws IOException {
        Request request = new Request.Builder().url(imageUrl).build();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

            // Создаем временный файл для изображения
            File tempFile = File.createTempFile("generated_image", ".png");
            try (InputStream in = response.body().byteStream();
                 FileOutputStream out = new FileOutputStream(tempFile)) {
                Files.copy(in, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
            return tempFile.getAbsolutePath(); // Возвращаем путь к локальному файлу
        }
    }

    private void sendImageToTelegram(long chatId, String imagePath, TelegramLongPollingBot bot) throws TelegramApiException {
        SendPhoto sendPhoto = new SendPhoto();
        sendPhoto.setChatId(String.valueOf(chatId));
        sendPhoto.setPhoto(new InputFile(new File(imagePath))); // Указываем локальный файл

        bot.execute(sendPhoto); // Отправляем изображение
    }
}