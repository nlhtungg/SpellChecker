import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class SpellChecker {

    private TrieNode root;
    private static final int NGRAM_SIZE = 2;

    public SpellChecker() {
        root = new TrieNode();
        try {
            loadDictionary("words.txt");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error loading dictionary: " + e.getMessage());
        }
        createAndShowGUI();
    }

    private void loadDictionary(String filePath) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        String line;
        while ((line = reader.readLine()) != null) {
            String word = line.trim().toLowerCase();
            addWord(word);
            addNgrams(word);
        }
        reader.close();
    }

    private void addWord(String word) {
        TrieNode node = root;
        for (char c : word.toCharArray()) {
            node = node.children.computeIfAbsent(c, k -> new TrieNode());
        }
        node.isEndOfWord = true;
    }

    private Map<String, Set<String>> ngramMap = new HashMap<>();

    private void addNgrams(String word) {
        for (int i = 0; i <= word.length() - NGRAM_SIZE; i++) {
            String ngram = word.substring(i, i + NGRAM_SIZE);
            ngramMap.computeIfAbsent(ngram, k -> new HashSet<>()).add(word);
        }
    }

    private String spellCheck(String text) {
        String[] words = text.split("\\s+");
        StringBuilder correctedText = new StringBuilder();

        for (String word : words) {
            if (containsWord(word.toLowerCase())) {
                correctedText.append(word).append(" ");
            } else {
                String suggestion = findClosestWord(word);
                correctedText.append(suggestion).append(" ");
            }
        }
        return correctedText.toString().trim();
    }

    private boolean containsWord(String word) {
        TrieNode node = root;
        for (char c : word.toCharArray()) {
            node = node.children.get(c);
            if (node == null) return false;
        }
        return node.isEndOfWord;
    }

    private String findClosestWord(String word) {
        Set<String> candidates = getNgramCandidates(word);
        String closestWord = word;
        int minDistance = Integer.MAX_VALUE;

        for (String candidate : candidates) {
            int distance = calculateWeightedEditDistance(word.toLowerCase(), candidate);
            if (distance < minDistance) {
                minDistance = distance;
                closestWord = candidate;
            }
        }
        return closestWord;
    }

    private Set<String> getNgramCandidates(String word) {
        Set<String> candidates = new HashSet<>();
        for (int i = 0; i <= word.length() - NGRAM_SIZE; i++) {
            String ngram = word.substring(i, i + NGRAM_SIZE);
            Set<String> similarWords = ngramMap.getOrDefault(ngram, Collections.emptySet());
            candidates.addAll(similarWords);
        }
        return candidates;
    }

    private int calculateWeightedEditDistance(String word1, String word2) {
        int len1 = word1.length();
        int len2 = word2.length();
        int[] prev = new int[len2 + 1];
        int[] curr = new int[len2 + 1];

        for (int j = 0; j <= len2; j++) {
            prev[j] = j;
        }

        for (int i = 1; i <= len1; i++) {
            curr[0] = i;
            for (int j = 1; j <= len2; j++) {
                int cost;
                if (word1.charAt(i - 1) == word2.charAt(j - 1)) {
                    cost = 0;
                } else if (areNearbyKeys(word1.charAt(i - 1), word2.charAt(j - 1))) {
                    cost = 1;
                } else {
                    cost = 2;
                }
                curr[j] = Math.min(Math.min(curr[j - 1] + 1, prev[j] + 1), prev[j - 1] + cost);
            }
            System.arraycopy(curr, 0, prev, 0, len2 + 1);
        }
        return curr[len2];
    }

    private boolean areNearbyKeys(char a, char b) {
        String nearbyKeys = "qwertyuioplkjhgfdsazxcvbnm";
        return Math.abs(nearbyKeys.indexOf(a) - nearbyKeys.indexOf(b)) <= 2;
    }

    private void createAndShowGUI() {
        JFrame frame = new JFrame("Spell Checker");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 300);
        frame.setLayout(new BorderLayout());

        JTextArea inputTextArea = new JTextArea(5, 30);
        inputTextArea.setLineWrap(true);
        inputTextArea.setWrapStyleWord(true);

        JTextArea outputTextArea = new JTextArea(5, 30);
        outputTextArea.setLineWrap(true);
        outputTextArea.setWrapStyleWord(true);
        outputTextArea.setEditable(false);

        JButton checkButton = new JButton("Check Spelling");
        checkButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String inputText = inputTextArea.getText();
                String correctedText = spellCheck(inputText);
                outputTextArea.setText(correctedText);
            }
        });

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JScrollPane(inputTextArea), BorderLayout.NORTH);
        panel.add(checkButton, BorderLayout.CENTER);
        panel.add(new JScrollPane(outputTextArea), BorderLayout.SOUTH);

        frame.add(panel);
        frame.setVisible(true);
    }

    static class TrieNode {
        Map<Character, TrieNode> children = new HashMap<>();
        boolean isEndOfWord = false;
    }
    // Main method
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SpellChecker());
    }
}
