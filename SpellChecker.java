import javax.swing.*; // Thư viện cho giao diện đồ họa
import java.awt.*; // Thư viện về bố cục giao diện
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

// Lớp chính thực hiện chức năng kiểm tra lỗi chính tả
public class SpellChecker {

    private TrieNode root; // Gốc của cây Trie lưu trữ từ điển
    private static final int NGRAM_SIZE = 2; // Kích thước N-gram để tạo các chuỗi con

    public SpellChecker() {
        root = new TrieNode(); // Khởi tạo cây Trie
        try {
            loadDictionary("words.txt"); // Nạp từ điển từ file "words.txt"
        } catch (IOException e) {
            // Thông báo lỗi nếu không thể nạp từ điển
            JOptionPane.showMessageDialog(null, "Error loading dictionary: " + e.getMessage());
        }
        createAndShowGUI(); // Tạo và hiển thị giao diện đồ họa
    }

    // Hàm nạp từ điển từ file
    private void loadDictionary(String filePath) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        String line;
        while ((line = reader.readLine()) != null) {
            String word = line.trim().toLowerCase(); // Chuyển từ về chữ thường và loại bỏ khoảng trắng
            addWord(word); // Thêm từ vào Trie
            addNgrams(word); // Tạo và thêm N-gram vào bản đồ N-gram
        }
        reader.close();
    }

    // Thêm một từ vào cây Trie
    private void addWord(String word) {
        TrieNode node = root;
        for (char c : word.toCharArray()) {
            node = node.children.computeIfAbsent(c, k -> new TrieNode()); // Thêm nút nếu chưa tồn tại
        }
        node.isEndOfWord = true; // Đánh dấu nút cuối là từ hoàn chỉnh
    }

    // Bản đồ lưu trữ các N-gram và từ liên quan
    private Map<String, Set<String>> ngramMap = new HashMap<>();

    // Tạo và thêm các N-gram của từ vào ngramMap
    private void addNgrams(String word) {
        for (int i = 0; i <= word.length() - NGRAM_SIZE; i++) {
            String ngram = word.substring(i, i + NGRAM_SIZE); // Tạo N-gram từ chuỗi con
            ngramMap.computeIfAbsent(ngram, k -> new HashSet<>()).add(word); // Lưu từ vào N-gram tương ứng
        }
    }

    // Hàm kiểm tra lỗi chính tả cho một đoạn văn bản
    private String spellCheck(String text) {
        String[] words = text.split("\\s+"); // Tách văn bản thành các từ
        StringBuilder correctedText = new StringBuilder();

        for (String word : words) {
            if (containsWord(word.toLowerCase())) {
                correctedText.append(word).append(" "); // Nếu từ đúng, giữ nguyên
            } else {
                String suggestion = findClosestWord(word); // Tìm từ gần đúng nhất
                correctedText.append(suggestion).append(" "); // Thay thế bằng từ gợi ý
            }
        }
        return correctedText.toString().trim(); // Trả về văn bản đã chỉnh sửa
    }

    // Kiểm tra từ có tồn tại trong Trie hay không
    private boolean containsWord(String word) {
        TrieNode node = root;
        for (char c : word.toCharArray()) {
            node = node.children.get(c);
            if (node == null) return false; // Nếu không tìm thấy ký tự, từ không tồn tại
        }
        return node.isEndOfWord; // Kiểm tra nếu nút cuối là từ hoàn chỉnh
    }

    // Tìm từ gần đúng nhất dựa trên khoảng cách chỉnh sửa
    private String findClosestWord(String word) {
        Set<String> candidates = getNgramCandidates(word); // Lấy danh sách từ ứng viên
        String closestWord = word;
        int minDistance = Integer.MAX_VALUE;

        for (String candidate : candidates) {
            int distance = calculateWeightedEditDistance(word.toLowerCase(), candidate); // Tính khoảng cách chỉnh sửa
            if (distance < minDistance) {
                minDistance = distance;
                closestWord = candidate; // Cập nhật từ gần đúng nhất
            }
        }
        return closestWord;
    }

    // Lấy các từ ứng viên từ bản đồ N-gram
    private Set<String> getNgramCandidates(String word) {
        Set<String> candidates = new HashSet<>();
        for (int i = 0; i <= word.length() - NGRAM_SIZE; i++) {
            String ngram = word.substring(i, i + NGRAM_SIZE);
            Set<String> similarWords = ngramMap.getOrDefault(ngram, Collections.emptySet()); // Lấy các từ tương ứng N-gram
            candidates.addAll(similarWords);
        }
        return candidates;
    }

    // Tính khoảng cách chỉnh sửa giữa hai từ
    private int calculateWeightedEditDistance(String word1, String word2) {
        int[][] dp = new int[word1.length() + 1][word2.length() + 1];

        for (int i = 0; i <= word1.length(); i++) {
            dp[i][0] = i;
        }

        for (int j = 0; j <= word2.length(); j++) {
            dp[0][j] = j;
        }

        for (int i = 1; i <= word1.length(); i++) {
            for (int j = 1; j <= word2.length(); j++) {
                int cost = word1.charAt(i - 1) == word2.charAt(j - 1) ? 0 : keyboardDistance(word1.charAt(i - 1), word2.charAt(j - 1));
                if (cost == Integer.MAX_VALUE) {
                    cost = 1; // Default cost if characters are not found on the keyboard
                }
                dp[i][j] = Math.min(Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1), dp[i - 1][j - 1] + cost);
            }
        }

        return dp[word1.length()][word2.length()];
    }
    
    // Tính toán khoảng cách giữa các phím trên bàn phím
    private int keyboardDistance(char c1, char c2) {
        int[][] keyboard = {
            {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10}, // QWERTYUIOP
            {0, 1, 2, 3, 4, 5, 6, 7, 8, 9},     // ASDFGHJKL
            {0, 1, 2, 3, 4, 5, 6, 7}            // ZXCVBNM
        };

        String[] rows = {
            "qwertyuiop",
            "asdfghjkl",
            "zxcvbnm"
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

    // Tạo và hiển thị giao diện người dùng
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
        checkButton.setPreferredSize(new Dimension(100, 30)); // Set the preferred size to make the button narrower
        checkButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String inputText = inputTextArea.getText();
                String correctedText = spellCheck(inputText);
                outputTextArea.setText(correctedText);
            }
        });

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.add(new JScrollPane(inputTextArea), BorderLayout.NORTH);
        panel.add(checkButton, BorderLayout.CENTER);
        panel.add(new JScrollPane(outputTextArea), BorderLayout.SOUTH);

        frame.add(panel);
        frame.setVisible(true);
    }

    // Lớp TrieNode để lưu trữ cấu trúc Trie
    static class TrieNode {
        Map<Character, TrieNode> children = new HashMap<>();
        boolean isEndOfWord = false;
    }

    // Hàm main để chạy chương trình
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SpellChecker());
    }
}
