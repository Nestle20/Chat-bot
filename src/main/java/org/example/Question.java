package org.example;

public class Question {
    private String text;
    private int score;

    public Question(String text, int score) {
        this.text = text;
        this.score = score;
    }

    public String getText() {
        return text;
    }

    public int getScore() {
        return score;
    }
}