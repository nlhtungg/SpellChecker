package src;

import src.model.Dictionary;
import src.service.SpellCheckService;
import src.view.SpellCheckerGUI;

import javax.swing.*;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                // Initialize the dictionary
                Dictionary dictionary = new Dictionary();
                dictionary.loadDictionary("words.txt");
                
                // Initialize the spell check service
                SpellCheckService spellCheckService = new SpellCheckService(dictionary);
                
                // Create and show the GUI
                new SpellCheckerGUI(spellCheckService);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, "Error loading dictionary: " + e.getMessage());
            }
        });
    }
}
