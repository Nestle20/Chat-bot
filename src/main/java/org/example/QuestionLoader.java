package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class QuestionLoader {
    public static List<Question> loadQuestionsFromCSV() throws IOException {
        List<Question> questions = new ArrayList<>();

        try (InputStream is = QuestionLoader.class.getResourceAsStream("/tiss_questions.csv");
             BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {

            if (is == null) {
                throw new IOException("Файл с вопросами не найден! Убедитесь, что tiss_questions.csv находится в папке resources");
            }

            // Пропускаем заголовок
            br.readLine();

            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(";");
                if (parts.length == 2) {
                    questions.add(new Question(parts[0].trim(), Integer.parseInt(parts[1].trim())));
                }
            }
        }
        return questions;
    }
}