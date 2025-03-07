package src.view;

import javax.swing.*;
import java.util.List;

public class SuggestionDialog {
    
    public static String showSuggestionDialog(String word, List<String> suggestions) {
        String[] options = suggestions.toArray(new String[0]);
        String selectedWord = (String) JOptionPane.showInputDialog(
                null,
                "The word \"" + word + "\" is misspelled. Please choose a suggestion:",
                "Spell Checker Suggestions",
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]
        );
        return selectedWord != null ? selectedWord : word;
    }
}