package telegrambot;

import com.example.telegrambot.ChatGPTBot;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@SpringBootApplication
public class ChatGPTBotApplication {

    public static void main(String[] args) {
        SpringApplication.run(ChatGPTBotApplication.class, args);

        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(new ChatGPTBot()); // Регистрируем бота
        } catch (TelegramApiException e) {
            e.printStackTrace(); // Обработка ошибки при регистрации бота
        }
    }
}
