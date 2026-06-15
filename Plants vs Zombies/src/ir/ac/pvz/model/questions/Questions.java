package ir.ac.pvz.model.questions;

import java.util.List;

public class Questions {

    private Questions() {}

    private static List<String> questionsList = List.of(
            "What is your favorite color?",
            "What was the name of your first pet?",
            "What city were you born in?",
            "What was the name of your elementary school?",
            "What was the make and model of your first car?",
            "What is the name of your favorite childhood friend?",
            "What is your favorite movie?",
            "What is the name of your favorite sports team?",
            "What was your favorite subject in school?"
    );

    public static List<String> getQuestionsList() {
        return questionsList;
    }
}
