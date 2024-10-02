package com.example.telegrambot;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class ChatGPTBot extends TelegramLongPollingBot {
    private ChatGPTService chatGPTService;
    private String botToken;
    private String botUsername;
    private String openAiApiKey;

    public ChatGPTBot() {
        ConfigLoader configLoader = new ConfigLoader(); // Инициализируем ConfigLoader
        this.botToken = configLoader.getBotToken();
        this.botUsername = configLoader.getBotUsername();
        this.openAiApiKey = configLoader.getOpenAiApiKey();
        chatGPTService = new ChatGPTService(openAiApiKey); // Инициализируем ChatGPTService
    }

    @Override
    public void onUpdateReceived(Update update) {
        // Проверяем, есть ли сообщение и текст сообщения
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            // Получаем ответ от ChatGPT
            String responseText = chatGPTService.getChatGPTResponse(messageText);

            // Создаем ответное сообщение
            SendMessage message = new SendMessage();
            message.setChatId(String.valueOf(chatId));
            message.setText(responseText);

            try {
                execute(message); // Отправляем сообщение пользователю
            } catch (TelegramApiException e) {
                e.printStackTrace(); // Логируем ошибку, если не удалось отправить сообщение
            }
        }
    }

    @Override
    public String getBotUsername() {
        return botUsername; // Возвращаем имя бота
    }

    @Override
    public String getBotToken() {
        return botToken; // Возвращаем токен бота
    }
}