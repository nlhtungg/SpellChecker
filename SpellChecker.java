import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Collections;

public class SpellChecker {

    // Class representing a Trie Node
    static class TrieNode {
        Map<Character, TrieNode> children = new HashMap<>();
        boolean isEndOfWord = false;
    }

    private TrieNode root; // Root of the Trie
    private static final int NGRAM_SIZE = 3; // Size of N-grams
    private Map<String, Set<String>> ngramMap = new HashMap<>(); // Map to store N-grams

    public SpellChecker() {
        root = new TrieNode();
        try {
            loadDictionary("words.txt");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error loading dictionary: " + e.getMessage());
        }
        createAndShowGUI();
    }

    // Load dictionary from file
    private void loadDictionary(String filePath) throws IOException {
        System.out.println("Loading dictionary from: " + filePath);
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        String line;
        while ((line = reader.readLine()) != null) {
            String word = line.trim().toLowerCase();
            addWord(word); // Add word to Trie
            addNgrams(word); // Generate and store N-grams
        }
        reader.close();
        System.out.println("Dictionary loaded successfully.");
    }

    // Add word to Trie
    private void addWord(String word) {
        TrieNode node = root;
        for (char c : word.toCharArray()) {
            node = node.children.computeIfAbsent(c, k -> new TrieNode());
        }
        node.isEndOfWord = true;
    }

    // Generate N-grams and store them in the map
    private void addNgrams(String word) {
        for (int i = 0; i <= word.length() - NGRAM_SIZE; i++) {
            String ngram = word.substring(i, i + NGRAM_SIZE);
            ngramMap.computeIfAbsent(ngram, k -> new HashSet<>()).add(word);
        }
    }

    // Spell-check a text
    private String spellCheck(String text) {
        String[] words = text.split("\\s+");
        StringBuilder correctedText = new StringBuilder();

        for (String word : words) {
            if (containsWord(word.toLowerCase())) {
                correctedText.append(word).append(" ");
            } else {
                Set<String> candidates = getNgramCandidates(word);
                if (candidates.isEmpty()) {
                    correctedText.append(word).append(" "); // No suggestions, keep original
                } else {
                    List<String> topCandidates = new ArrayList<>(candidates);
                    topCandidates.sort(Comparator.comparingInt(candidate -> calculateWeightedEditDistance(word.toLowerCase(), candidate)));
                    topCandidates = topCandidates.subList(0, Math.min(10, topCandidates.size()));

                    // Show suggestions and get user choice
                    String suggestion = showSuggestionDialog(word, topCandidates);
                    correctedText.append(suggestion).append(" ");
                }
            }
        }
        return correctedText.toString().trim();
    }

    // Show suggestions in a dialog box
    private String showSuggestionDialog(String word, List<String> suggestions) {
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
        return selectedWord != null ? selectedWord : word; // Fallback to original word if user cancels
    }

    // Check if a word exists in the Trie
    private boolean containsWord(String word) {
        TrieNode node = root;
        for (char c : word.toCharArray()) {
            node = node.children.get(c);
            if (node == null) return false;
        }
        return node.isEndOfWord;
    }

    // Get candidate words using N-grams
    private Set<String> getNgramCandidates(String word) {
        Set<String> candidates = new HashSet<>();
        for (int i = 0; i <= word.length() - NGRAM_SIZE; i++) {
            String ngram = word.substring(i, i + NGRAM_SIZE);
            candidates.addAll(ngramMap.getOrDefault(ngram, Collections.emptySet()));
        }
        System.out.println("Number of candidates for word \"" + word + "\": " + candidates.size());
        return candidates;
    }

    // Calculate weighted edit distance
    private int calculateWeightedEditDistance(String word1, String word2) {
        int[][] dp = new int[word1.length() + 1][word2.length() + 1];

        for (int i = 0; i <= word1.length(); i++) dp[i][0] = i;
        for (int j = 0; j <= word2.length(); j++) dp[0][j] = j;

        for (int i = 1; i <= word1.length(); i++) {
            for (int j = 1; j <= word2.length(); j++) {
                int cost = word1.charAt(i - 1) == word2.charAt(j - 1) ? 0 : keyboardDistance(word1.charAt(i - 1), word2.charAt(j - 1));
                dp[i][j] = Math.min(dp[i - 1][j] + 1,
                        Math.min(dp[i][j - 1] + 1, dp[i - 1][j - 1] + cost));
            }
        }
        return dp[word1.length()][word2.length()];
    }

    // Tính toán khoảng cách giữa các phím trên bàn phím
    private int keyboardDistance(char c1, char c2) {
        String[] rows = {
            "1234567890-=",
            "qwertyuiop[]",
            "asdfghjkl;'",
            "zxcvbnm,./"
        };

        int[] pos1 = findPosition(rows, c1);
        int[] pos2 = findPosition(rows, c2);

        if (pos1 == null || pos2 == null) {
            return Integer.MAX_VALUE; // Characters not found on keyboard
        }
        return Math.abs(pos1[0] - pos2[0]) + Math.abs(pos1[1] - pos2[1]);
    }

    private int[] findPosition(String[] rows, char c) {
        for (int i = 0; i < rows.length; i++) {
            int index = rows[i].indexOf(c);
            if (index != -1) {
                return new int[]{i, index};
            }
        }
        return null;
    }


    // Create and display the GUI
    private void createAndShowGUI() {
        JFrame frame = new JFrame("Spell Checker");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);
        frame.setLayout(new BorderLayout(10, 10));

        JTextArea inputTextArea = new JTextArea(5, 30);
        inputTextArea.setLineWrap(true);
        inputTextArea.setWrapStyleWord(true);
        inputTextArea.setBorder(BorderFactory.createTitledBorder("Input Text"));

        JTextArea outputTextArea = new JTextArea(5, 30);
        outputTextArea.setLineWrap(true);
        outputTextArea.setWrapStyleWord(true);
        outputTextArea.setEditable(false);
        outputTextArea.setBorder(BorderFactory.createTitledBorder("Corrected Text"));

        JButton checkButton = new JButton("Check");
        checkButton.setFont(new Font("Arial", Font.BOLD, 14));
        checkButton.setBackground(new Color(70, 130, 180));
        checkButton.setForeground(Color.WHITE);
        checkButton.setFocusPainted(false);
        checkButton.setPreferredSize(new Dimension(100, 30));
        checkButton.addActionListener((ActionEvent e) -> {
            String inputText = inputTextArea.getText();
            String correctedText = spellCheck(inputText);
            outputTextArea.setText(correctedText);
        });

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.add(new JScrollPane(inputTextArea), BorderLayout.NORTH);
        panel.add(checkButton, BorderLayout.CENTER);
        panel.add(new JScrollPane(outputTextArea), BorderLayout.SOUTH);

        frame.add(panel);
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(SpellChecker::new);
    }
}
