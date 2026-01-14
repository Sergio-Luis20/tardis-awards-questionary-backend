package br.sergio.tardis_awards_questionary.questionary;

import br.sergio.tardis_awards_questionary.discord.MemberResponse;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public record Question(int id, String question, String description, int numAnswers, Set<MemberResponse> options) {

    private static volatile List<Question> questions;

    public static List<Question> questions() {
        return questions;
    }

    public static synchronized void setQuestions(List<Question> questions) {
        if (Question.questions != null) {
            throw new IllegalStateException("Questions can be defined only 1 time");
        }
        Question.questions = Collections.unmodifiableList(questions);
    }

}
