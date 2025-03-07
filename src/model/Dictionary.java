package src.model;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class Dictionary {
    // CTDL Trie để lưu từ điển
    private TrieNode root; // Root of the Trie
    private static final int NGRAM_SIZE = 3; // Size of N-grams
    private Map<String, Set<String>> ngramMap = new HashMap<>(); // Map to store N-grams

    public Dictionary() {
        root = new TrieNode();
    }

    // Load dictionary from file
    public void loadDictionary(String filePath) throws IOException {
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
    public void addWord(String word) {
        TrieNode node = root;
        for (char c : word.toCharArray()) {
            node = node.children.computeIfAbsent(c, k -> new TrieNode());
        }
        node.isEndOfWord = true;
    }

    // Tạo và lưu N-grams cho từ
    private void addNgrams(String word) {
        for (int i = 0; i <= word.length() - NGRAM_SIZE; i++) {
            String ngram = word.substring(i, i + NGRAM_SIZE);
            ngramMap.computeIfAbsent(ngram, k -> new HashSet<>()).add(word);
        }
    }

    // Kiểm tra xem từ có trong từ điển không
    public boolean containsWord(String word) {
        TrieNode node = root;
        for (char c : word.toCharArray()) {
            node = node.children.get(c);
            if (node == null) return false;
        }
        return node.isEndOfWord;
    }

    // Lấy tập hợp các từ gợi ý dựa trên N-grams
    public Set<String> getNgramCandidates(String word) {
        Set<String> candidates = new HashSet<>();
        for (int i = 0; i <= word.length() - NGRAM_SIZE; i++) {
            if (i + NGRAM_SIZE <= word.length()) {
                String ngram = word.substring(i, i + NGRAM_SIZE);
                candidates.addAll(ngramMap.getOrDefault(ngram, Collections.emptySet()));
            }
        }
        System.out.println("Number of candidates for word \"" + word + "\": " + candidates.size());
        return candidates;
    }
}