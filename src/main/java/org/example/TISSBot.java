package org.example;

import io.github.exortions.dotenv.DotEnv;
import io.github.exortions.dotenv.EnvParameterNotFoundException;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TISSBot extends TelegramLongPollingBot {
    private final Map<Long, Integer> userScores;
    private final Map<Long, Integer> currentQuestion;
    private final List<Question> questions;

    public TISSBot() throws IOException {
        this.userScores = new HashMap<>();
        this.currentQuestion = new HashMap<>();
        this.questions = QuestionLoader.loadQuestionsFromCSV();
    }

    @Override
    public String getBotUsername() {
        DotEnv dotEnv = new DotEnv(new File(".env"));
        dotEnv.loadParams();
        try {
            return dotEnv.getParameter("BOT_NAME");
        } catch (EnvParameterNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getBotToken() {
        DotEnv dotEnv = new DotEnv(new File(".env"));
        dotEnv.loadParams();
        try {
            return dotEnv.getParameter("BOT_TOKEN");
        } catch (EnvParameterNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            Message message = update.getMessage();
            long chatId = message.getChatId();
            String text = message.getText();

            SendMessage response = new SendMessage();
            response.setChatId(String.valueOf(chatId));

            if (text.equals("/start")) {
                startTesting(chatId, response);
            } else if (text.equals("/help")) {
                response.setText("Этот бот оценивает ваше состояние по шкале TISS.\n\n"
                        + "Команды:\n"
                        + "/start - начать тестирование\n"
                        + "/help - показать эту справку\n\n"
                        + "Отвечайте на вопросы цифрой:\n"
                        + "1 - если вмешательство БЫЛО\n"
                        + "0 - если вмешательства НЕ БЫЛО");
            } else if (currentQuestion.containsKey(chatId)) {
                processAnswer(chatId, text, response);
            } else {
                response.setText("Неизвестная команда. Введите /start для начала тестирования или /help для справки.");
            }

            try {
                execute(response);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }

    private void startTesting(long chatId, SendMessage response) {
        userScores.put(chatId, 0);
        currentQuestion.put(chatId, 0);
        response.setText("Оценка состояния по шкале TISS\n\n"
                + "Отвечайте на вопросы цифрой:\n"
                + "1 - если вмешательство БЫЛО\n"
                + "0 - если вмешательства НЕ БЫЛО\n\n"
                + questions.get(0).getText());
    }

    private void processAnswer(long chatId, String answer, SendMessage response) {
        try {
            int score = Integer.parseInt(answer);
            if (score != 0 && score != 1) {
                response.setText("Пожалуйста, введите 1 (если было) или 0 (если не было)");
                return;
            }

            int currentQ = currentQuestion.get(chatId);
            int totalScore = userScores.get(chatId);

            if (score == 1) {
                totalScore += questions.get(currentQ).getScore();
                userScores.put(chatId, totalScore);
            }

            currentQ++;
            if (currentQ < questions.size()) {
                currentQuestion.put(chatId, currentQ);
                response.setText(questions.get(currentQ).getText());
            } else {
                currentQuestion.remove(chatId);
                String result = evaluateResults(totalScore);
                response.setText("Тестирование завершено.\n\n"
                        + "Ваш общий балл: " + totalScore + "\n\n"
                        + result + "\n\n"
                        + "Для нового тестирования введите /start");
            }
        } catch (NumberFormatException e) {
            response.setText("Пожалуйста, введите 1 (если было) или 0 (если не было)");
        }
    }

    private String evaluateResults(int score) {
        double expectedTISS = (score - 3.33) / 0.97;
        String interpretation;
        String treatment;

        if (score <= 5) {
            interpretation = "I класс тяжести - ЛЁГКОЕ СОСТОЯНИЕ";
            treatment = "Рекомендации:\n"
                    + "- Стандартное наблюдение\n"
                    + "- Регулярный контроль показателей\n"
                    + "- Поддерживающая терапия";
        } else if (score <= 15) {
            interpretation = "II класс тяжести - СРЕДНЯЯ ТЯЖЕСТЬ";
            treatment = "Рекомендации:\n"
                    + "- Усиленное наблюдение\n"
                    + "- Поддержка жизненных функций\n"
                    + "- Медикаментозная терапия";
        } else if (score <= 25) {
            interpretation = "III класс тяжести - ТЯЖЁЛОЕ СОСТОЯНИЕ";
            treatment = "Рекомендации:\n"
                    + "- Интенсивная терапия\n"
                    + "- Постоянный мониторинг\n"
                    + "- Специализированные процедуры";
        } else {
            interpretation = "IV класс тяжести - КРАЙНЕ ТЯЖЁЛОЕ СОСТОЯНИЕ";
            treatment = "Рекомендации:\n"
                    + "- Реанимационные мероприятия\n"
                    + "- Непрерывное наблюдение\n"
                    + "- Комплексное лечение";
        }

        return interpretation + "\n\n" + treatment;

    }

    @Override
    public void clearWebhook() {
        // Пустая реализация
    }
}