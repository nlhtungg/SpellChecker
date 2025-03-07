package src.util;

public class EditDistanceCalculator {
    
    // Tính toán khoảng cách chỉnh sửa có trọng số giữa hai từ
    public static int calculateWeightedEditDistance(String word1, String word2) {
        int[][] dp = new int[word1.length() + 1][word2.length() + 1];

        for (int i = 0; i <= word1.length(); i++) dp[i][0] = i;
        for (int j = 0; j <= word2.length(); j++) dp[0][j] = j;

        for (int i = 1; i <= word1.length(); i++) {
            for (int j = 1; j <= word2.length(); j++) {
                int cost = word1.charAt(i - 1) == word2.charAt(j - 1) ? 0 : keyboardDistance(word1.charAt(i - 1), word2.charAt(j - 1));
                dp[i][j] = Math.min(dp[i - 1][j] + 1,
                        Math.min(dp[i][j - 1] + 1, dp[i - 1][j - 1] + cost));
                        
                // Thêm trọng số cho các phép thay thế
                if (i > 1 && j > 1 && word1.charAt(i - 1) == word2.charAt(j - 2) && word1.charAt(i - 2) == word2.charAt(j - 1)) {
                    dp[i][j] = Math.min(dp[i][j], dp[i - 2][j - 2] + cost);
                }
            }
        }
        return dp[word1.length()][word2.length()];
    }

    // Tính toán khoảng cách giữa các phím trên bàn phím
    private static int keyboardDistance(char c1, char c2) {
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

    private static int[] findPosition(String[] rows, char c) {
        for (int i = 0; i < rows.length; i++) {
            int index = rows[i].indexOf(c);
            if (index != -1) {
                return new int[]{i, index};
            }
        }
        return null;
    }
}