//dependencies: gson-2.8.6.jar (or later)

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.*;
import javax.swing.Timer;

class Question {
    String question;
    String option_a;
    String option_b;
    String option_c;
    String option_d;
    String answer;
}

public class BibleQuizApp extends JFrame {
    private List<Question> allQuestions;
    private List<Question> quizQuestions;
    private int currentQuestionIndex = 0;
    private int score = 0;
    private JLabel questionLabel, timerLabel;
    private JButton[] optionButtons = new JButton[4];
    private Timer countdownTimer;
    private int timeLeft = 30;

    public BibleQuizApp() {
        setTitle("Bible Quiz App");
        setSize(600, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        questionLabel = new JLabel("Question", SwingConstants.CENTER);
        questionLabel.setFont(new Font("Arial", Font.BOLD, 18));

        JPanel optionsPanel = new JPanel(new GridLayout(2, 2));
        for (int i = 0; i < 4; i++) {
            optionButtons[i] = new JButton();
            optionButtons[i].setFont(new Font("Arial", Font.PLAIN, 16));
            int finalI = i;
            optionButtons[i].addActionListener(e -> checkAnswer(optionButtons[finalI]));
            optionsPanel.add(optionButtons[i]);
        }

        timerLabel = new JLabel("Time: 30", SwingConstants.CENTER);
        timerLabel.setFont(new Font("Arial", Font.BOLD, 16));

        add(questionLabel, BorderLayout.NORTH);
        add(optionsPanel, BorderLayout.CENTER);
        add(timerLabel, BorderLayout.SOUTH);

        loadQuestions();
        startQuiz();
    }

    private void loadQuestions() {
        try {
            String json = new String(Files.readAllBytes(Paths.get("questions.json")));
            java.lang.reflect.Type questionListType = new TypeToken<ArrayList<Question>>() {}.getType();
            allQuestions = new Gson().fromJson(json, questionListType);
            Collections.shuffle(allQuestions);
            quizQuestions = allQuestions.subList(0, 15);
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to load questions.", "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    private void startQuiz() {
        currentQuestionIndex = 0;
        score = 0;
        nextQuestion();
    }

    private void nextQuestion() {
        if (currentQuestionIndex >= quizQuestions.size()) {
            endQuiz();
            return;
        }

        Question q = quizQuestions.get(currentQuestionIndex);
        questionLabel.setText("Q" + (currentQuestionIndex + 1) + ": " + q.question);
        optionButtons[0].setText(q.option_a);
        optionButtons[1].setText(q.option_b);
        optionButtons[2].setText(q.option_c);
        optionButtons[3].setText(q.option_d);

        for (JButton button : optionButtons) {
            button.setEnabled(true);
            button.setBackground(null);
        }

        timeLeft = 30;
        timerLabel.setText("Time: " + timeLeft);

        if (countdownTimer != null) countdownTimer.stop();

        countdownTimer = new Timer(1000, new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                timeLeft--;
                timerLabel.setText("Time: " + timeLeft);
                if (timeLeft <= 0) {
                    countdownTimer.stop();
                    JOptionPane.showMessageDialog(BibleQuizApp.this, "Time's up! Quiz over.");
                    endQuiz();
                }
            }
        });
        countdownTimer.start();
    }

    private void checkAnswer(JButton selectedButton) {
        countdownTimer.stop();
        Question current = quizQuestions.get(currentQuestionIndex);
        String selectedText = selectedButton.getText();

        if (selectedText.equalsIgnoreCase(current.answer)) {
            selectedButton.setBackground(Color.GREEN);
            score++;
        } else {
            selectedButton.setBackground(Color.RED);
            Timer blinkTimer = new Timer(150, new ActionListener() {
                int count = 0;
                public void actionPerformed(ActionEvent evt) {
                    if (count++ % 2 == 0) selectedButton.setBackground(Color.RED);
                    else selectedButton.setBackground(null);
                    if (count > 4) ((Timer) evt.getSource()).stop();
                }
            });
            blinkTimer.start();
        }

        for (JButton button : optionButtons) button.setEnabled(false);

        Timer pause = new Timer(1500, e -> {
            currentQuestionIndex++;
            nextQuestion();
        });
        pause.setRepeats(false);
        pause.start();
    }

    private void endQuiz() {
        JOptionPane.showMessageDialog(this, "Quiz Over! Your score: " + score + "/15");
        System.exit(0);
    }

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {
            new BibleQuizApp().setVisible(true);
        });
    }
}
