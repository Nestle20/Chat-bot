package org.example;

import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(new TISSBot());
            System.out.println("Бот успешно запущен!");
        } catch (TelegramApiException | IOException e) {
            System.err.println("Ошибка при запуске бота:");
            e.printStackTrace();
        }
    }
}