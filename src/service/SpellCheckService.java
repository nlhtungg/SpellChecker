package src.service;

import src.model.Dictionary;
import src.util.EditDistanceCalculator;
import src.view.SuggestionDialog;

import java.util.*;

public class SpellCheckService {
    
    private Dictionary dictionary;
    
    public SpellCheckService(Dictionary dictionary) {
        this.dictionary = dictionary;
    }
    
    // Kiểm tra và sửa lỗi chính tả trong văn bản
    public String spellCheck(String text) {
        String[] words = text.split("\\s+");
        StringBuilder correctedText = new StringBuilder();

        for (String word : words) {
            // Skip empty words
            if (word.isEmpty()) {
                continue;
            }
            
            if (dictionary.containsWord(word.toLowerCase())) {
                correctedText.append(word).append(" ");
            } else {
                Set<String> candidates = dictionary.getNgramCandidates(word); // Lấy các ứng viên gợi ý dựa trên N-grams
                if (candidates.isEmpty()) {
                    correctedText.append(word).append(" "); // Không có từ gợi ý, giữ nguyên từ
                } else {
                    List<String> topCandidates = new ArrayList<>(candidates);
                    topCandidates.sort(Comparator.comparingInt(candidate 
                    -> EditDistanceCalculator.calculateWeightedEditDistance(word.toLowerCase(), candidate)));
                    topCandidates = topCandidates.subList(0, Math.min(10, topCandidates.size()));

                    // In ra khoảng cách chỉnh sửa giữa từ và các từ gợi ý
                    // System.out.println("Edit distance between \"" + word + "\" and suggestions:");
                    // for (String candidate : topCandidates) {
                    //    int distance = EditDistanceCalculator.calculateWeightedEditDistance(word.toLowerCase(), candidate);
                    //    System.out.println(candidate + ": " + distance);
                    //}

                    // Hiển thị hộp thoại gợi ý
                    String suggestion = SuggestionDialog.showSuggestionDialog(word, topCandidates);
                    correctedText.append(suggestion).append(" ");
                }
            }
        }
        return correctedText.toString().trim();
    }
}