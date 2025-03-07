package src.view;

import src.service.SpellCheckService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class SpellCheckerGUI {
    
    private JFrame frame;
    private JTextArea inputTextArea;
    private JTextArea outputTextArea;
    private SpellCheckService spellCheckService;
    
    public SpellCheckerGUI(SpellCheckService spellCheckService) {
        this.spellCheckService = spellCheckService;
        createAndShowGUI();
    }
    
    // Create and display the GUI
    private void createAndShowGUI() {
        frame = new JFrame("Spell Checker");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);
        frame.setLayout(new BorderLayout(10, 10));

        inputTextArea = new JTextArea(5, 30);
        inputTextArea.setLineWrap(true);
        inputTextArea.setWrapStyleWord(true);
        inputTextArea.setBorder(BorderFactory.createTitledBorder("Input Text"));

        outputTextArea = new JTextArea(5, 30);
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
            String correctedText = spellCheckService.spellCheck(inputText);
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
}