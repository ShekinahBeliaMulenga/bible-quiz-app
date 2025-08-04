package src;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.animation.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.media.AudioClip;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class BibleQuizApp extends Application {

    private List<Question> allQuestions;
    private List<Question> quizQuestions;
    private Set<Integer> usedQuestionIndices = new HashSet<>();
    private int currentQuestionIndex = 0;
    private int score = 0;

    private Label questionLabel;
    private Label timerLabel;
    private Label timerIcon;
    private Label scoreLabel;
    private Label questionCounter;
    private Button[] optionButtons = new Button[4];
    private Timeline countdownTimeline;
    private int timeLeft = 30;
    private ProgressBar timerProgress;

    // Sound effects
    private AudioClip correctSound;
    private AudioClip wrongSound;
    private AudioClip timeUpSound;

    // GUI Components
    private Scene scene;
    private StackPane welcomeLayout;
    private StackPane quizLayout;
    private StackPane scoreLayout;
    private Stage primaryStage;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.setTitle("Bible Quiz");
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(700);

        // Initialize sound effects
        initializeSounds();

        // Create welcome screen
        createWelcomeScreen();

        // Load questions
        loadQuestions();
    }

    private void initializeSounds() {
        try {
            correctSound = loadSound("resources/sounds/correct.wav");
            wrongSound = loadSound("resources/sounds/wrong.wav");
            timeUpSound = loadSound("resources/sounds/timeup.wav");
        } catch (Exception e) {
            System.err.println("Error loading sounds: " + e.getMessage());
        }
    }

    private AudioClip loadSound(String filePath) {
        try {
            return new AudioClip(getClass().getResource("/" + filePath).toString());
        } catch (Exception e) {
            System.err.println("Error loading sound: " + filePath);
            return null;
        }
    }

    private void createWelcomeScreen() {
        // Background with gradient
        Pane backgroundPane = new Pane();
        backgroundPane.setStyle(
                "-fx-background-color: linear-gradient(to bottom right, #a8c0ff, #3f2b96);"
        );

        // Main content container
        VBox mainContent = new VBox(40);
        mainContent.setAlignment(Pos.CENTER);
        mainContent.setPadding(new Insets(50));

        // Bible icon
        Label bibleIcon = new Label("📖");
        bibleIcon.setStyle("-fx-font-size: 120px;");

        // Title with glow effect
        Label titleLabel = new Label("Bible Quiz");
        titleLabel.setStyle(
                "-fx-font-size: 42px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-text-fill: white; " +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.8), 10, 0, 0, 0);"
        );

        // Subtitle
        Label subtitleLabel = new Label("Test your knowledge of Scripture");
        subtitleLabel.setStyle(
                "-fx-font-size: 18px; " +
                        "-fx-text-fill: rgba(255,255,255,0.9); " +
                        "-fx-font-style: italic;"
        );

        // Start button
        Button startButton = new Button("START QUIZ");
        startButton.setStyle(
                "-fx-font-size: 18px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-padding: 15 40; " +
                        "-fx-background-radius: 30; " +
                        "-fx-background-color: linear-gradient(to bottom, #4CAF50, #2E7D32); " +
                        "-fx-text-fill: white; " +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.4), 10, 0, 0, 5);"
        );

        // Button hover effects
        startButton.setOnMouseEntered(e -> {
            startButton.setStyle(
                    "-fx-font-size: 18px; " +
                            "-fx-font-weight: bold; " +
                            "-fx-padding: 15 40; " +
                            "-fx-background-radius: 30; " +
                            "-fx-background-color: linear-gradient(to bottom, #66BB6A, #388E3C); " +
                            "-fx-text-fill: white; " +
                            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.6), 15, 0, 0, 8);"
            );
        });

        startButton.setOnMouseExited(e -> {
            startButton.setStyle(
                    "-fx-font-size: 18px; " +
                            "-fx-font-weight: bold; " +
                            "-fx-padding: 15 40; " +
                            "-fx-background-radius: 30; " +
                            "-fx-background-color: linear-gradient(to bottom, #4CAF50, #2E7D32); " +
                            "-fx-text-fill: white; " +
                            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.4), 10, 0, 0, 5);"
            );
        });

        startButton.setOnAction(e -> startQuiz());

        // Stats preview
        VBox statsBox = new VBox(15);
        statsBox.setAlignment(Pos.CENTER);
        statsBox.setMaxWidth(400);
        statsBox.setStyle(
                "-fx-background-color: rgba(0,0,0,0.2); " +
                        "-fx-background-radius: 15; " +
                        "-fx-padding: 20; " +
                        "-fx-border-color: rgba(255,255,255,0.3); " +
                        "-fx-border-radius: 15; " +
                        "-fx-border-width: 2;"
        );

        Label statsTitle = new Label("QUIZ DETAILS");
        statsTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: white;");

        Label questionCount = new Label("• 15 Random Questions");
        Label timeLimit = new Label("• 30 Seconds Per Question");
        Label difficulty = new Label("• Biblical Knowledge Test");

        String statStyle = "-fx-font-size: 14px; -fx-text-fill: white;";
        questionCount.setStyle(statStyle);
        timeLimit.setStyle(statStyle);
        difficulty.setStyle(statStyle);

        statsBox.getChildren().addAll(statsTitle, questionCount, timeLimit, difficulty);

        mainContent.getChildren().addAll(bibleIcon, titleLabel, subtitleLabel, startButton, statsBox);

        welcomeLayout = new StackPane();
        welcomeLayout.getChildren().addAll(backgroundPane, mainContent);

        scene = new Scene(welcomeLayout, 800, 700);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void createQuizScreen() {
        // Clear previous quiz layout if exists
        if (quizLayout != null) {
            quizLayout.getChildren().clear();
        }

        // Updated background to lighter color
        Pane backgroundPane = new Pane();
        backgroundPane.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #f5f7fa, #e4e8f0);"
        );

        // Main layout using BorderPane for better control
        BorderPane mainLayout = new BorderPane();
        mainLayout.setPadding(new Insets(10));

        // Create top bar with home button, timer and score
        HBox topBar = new HBox(20);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setStyle("-fx-background-color: rgba(255,255,255,0.7); -fx-background-radius: 10; -fx-padding: 10;");
        topBar.setMaxWidth(Double.MAX_VALUE);

        // Timer progress bar
        timerProgress = new ProgressBar(1.0);
        timerProgress.setPrefWidth(250);
        timerProgress.setPrefHeight(10);
        timerProgress.setStyle(
                "-fx-accent: #4CAF50; " +
                        "-fx-background-radius: 5; " +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 1);"
        );

        // Timer container
        HBox timerContainer = new HBox(5);
        timerContainer.setAlignment(Pos.CENTER);

        timerIcon = new Label("⏳");
        timerIcon.setStyle("-fx-font-size: 20px;");

        timerLabel = new Label("30 SECONDS");
        timerLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));

        timerContainer.getChildren().addAll(timerIcon, timerLabel);

        // Score label
        scoreLabel = new Label("🏆 SCORE: 0");
        scoreLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));

        // Add spacer to push items to edges
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        topBar.getChildren().addAll( timerProgress, timerContainer, spacer, scoreLabel);
        mainLayout.setTop(topBar);

        // Center content - question and options
        VBox centerContent = new VBox(20);
        centerContent.setAlignment(Pos.CENTER);
        centerContent.setPadding(new Insets(20));

        // Header with title
        HBox headerBox = new HBox(10);
        headerBox.setAlignment(Pos.CENTER);

        Label bibleIcon = new Label("📖");
        bibleIcon.setStyle("-fx-font-size: 40px;");

        Label titleLabel = new Label("BIBLE QUIZ");
        titleLabel.setStyle(
                "-fx-font-size: 28px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-text-fill: #37474f; " +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 1);"
        );

        headerBox.getChildren().addAll(bibleIcon, titleLabel);

        // Question card
        VBox questionCard = new VBox();
        questionCard.setAlignment(Pos.CENTER);
        questionCard.setPadding(new Insets(25));
        questionCard.setMaxWidth(650);
        questionCard.setStyle(
                "-fx-background-color: white; " +
                        "-fx-background-radius: 20; " +
                        "-fx-border-color: #cfd8dc; " +
                        "-fx-border-width: 1; " +
                        "-fx-border-radius: 20; " +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 15, 0, 0, 5);"
        );

        questionLabel = new Label();
        questionLabel.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        questionLabel.setWrapText(true);
        questionLabel.setAlignment(Pos.CENTER);
        questionLabel.setTextFill(Color.web("#37474f"));
        questionLabel.setStyle("-fx-line-spacing: 8px;");

        questionCard.getChildren().add(questionLabel);

        // Answer options
        GridPane optionsGrid = new GridPane();
        optionsGrid.setHgap(20);
        optionsGrid.setVgap(20);
        optionsGrid.setAlignment(Pos.CENTER);

        String optionColor = "#3F51B5";

        for (int i = 0; i < 4; i++) {
            Button btn = createOptionButton(optionColor);
            optionButtons[i] = btn;
            optionsGrid.add(btn, i % 2, i / 2);
        }

        centerContent.getChildren().addAll(headerBox, questionCard, optionsGrid);
        mainLayout.setCenter(centerContent);

        // Create footer with question counter
        HBox footer = new HBox();
        footer.setAlignment(Pos.CENTER);
        footer.setStyle("-fx-background-color: rgba(255,255,255,0.7); -fx-background-radius: 10; -fx-padding: 10;");

        questionCounter = new Label();
        questionCounter.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        questionCounter.setText("Question 1 of 15");

        // Add some styling to make it stand out
        questionCounter.setStyle(
                "-fx-text-fill: #3F51B5; " +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 5, 0, 0, 1);"
        );

        footer.getChildren().add(questionCounter);
        mainLayout.setBottom(footer);

        quizLayout = new StackPane();
        quizLayout.getChildren().addAll(backgroundPane, mainLayout);
    }

    private Button createOptionButton(String color) {
        Button btn = new Button();
        btn.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        btn.setPrefWidth(320);
        btn.setPrefHeight(70);
        btn.setStyle(
                "-fx-background-radius: 15; " +
                        "-fx-background-color: " + color + "; " +
                        "-fx-text-fill: white; " +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 10, 0, 0, 3);"
        );

        // Hover effects
        btn.setOnMouseEntered(e -> {
            if (!btn.isDisabled()) {
                btn.setStyle(
                        "-fx-background-radius: 15; " +
                                "-fx-background-color: derive(" + color + ", 20%); " +
                                "-fx-text-fill: white; " +
                                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 15, 0, 0, 5);"
                );
            }
        });

        btn.setOnMouseExited(e -> {
            if (!btn.isDisabled()) {
                btn.setStyle(
                        "-fx-background-radius: 15; " +
                                "-fx-background-color: " + color + "; " +
                                "-fx-text-fill: white; " +
                                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 10, 0, 0, 3);"
                );
            }
        });

        btn.setOnAction(e -> checkAnswer(btn));
        return btn;
    }

    private void loadQuestions() {
    try (InputStream is = getClass().getResourceAsStream("/data/questions.json")) {
        if (is == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "Questions file not found inside JAR.");
            Platform.exit();
            return;
        }

        InputStreamReader reader = new InputStreamReader(is);
        java.lang.reflect.Type questionListType = new TypeToken<ArrayList<Question>>() {}.getType();
        allQuestions = new Gson().fromJson(reader, questionListType);

        if (allQuestions.size() < 200) {
            showAlert(Alert.AlertType.WARNING, "Warning", "The questions file should contain at least 200 questions.");
        }
    } catch (Exception e) {
        e.printStackTrace();
        showAlert(Alert.AlertType.ERROR, "Error", "Failed to load questions.");
        Platform.exit();
    }
}

    private void startQuiz() {
        // Reset used questions if starting a new quiz
        usedQuestionIndices.clear();
        quizQuestions = new ArrayList<>();

        // Create a list of all available question indices
        List<Integer> allIndices = new ArrayList<>();
        for (int i = 0; i < allQuestions.size(); i++) {
            allIndices.add(i);
        }

        // Shuffle all indices to get random order
        Collections.shuffle(allIndices);

        // Select the first 15 unique questions (or less if not enough available)
        int quizSize = Math.min(15, allQuestions.size());
        for (int i = 0; i < quizSize; i++) {
            int randomIndex = allIndices.get(i);
            quizQuestions.add(allQuestions.get(randomIndex));
            usedQuestionIndices.add(randomIndex);
        }

        // Initialize quiz
        currentQuestionIndex = 0;
        score = 0;

        // Always create a new quiz screen
        createQuizScreen();

        scene.setRoot(quizLayout);
        nextQuestion();
    }

    private void nextQuestion() {
        if (currentQuestionIndex >= quizQuestions.size()) {
            endQuiz();
            return;
        }

        questionCounter.setText("Question " + (currentQuestionIndex + 1) + " of " + quizQuestions.size());

        Question q = quizQuestions.get(currentQuestionIndex);
        updateQuestionDisplay(q);

        resetButtonStyles();
        enableAllButtons();
        startTimer();
    }

    private void updateQuestionDisplay(Question q) {
        questionLabel.setText("Question " + (currentQuestionIndex + 1) + ": " + q.question);
        optionButtons[0].setText("A. " + q.option_a);
        optionButtons[1].setText("B. " + q.option_b);
        optionButtons[2].setText("C. " + q.option_c);
        optionButtons[3].setText("D. " + q.option_d);
    }

    private void startTimer() {
        timeLeft = 30;
        timerLabel.setText(timeLeft + " SECONDS");
        timerProgress.setProgress(1.0);

        // Reset styles first
        timerProgress.setStyle(
                "-fx-accent: #4CAF50; " +
                        "-fx-background-radius: 5; " +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 1);"
        );
        timerLabel.setTextFill(Color.BLACK);
        timerIcon.setStyle("-fx-font-size: 20px;");

        if (countdownTimeline != null) {
            countdownTimeline.stop();
        }

        countdownTimeline = new Timeline(new KeyFrame(Duration.seconds(1), ev -> {
            timeLeft--;
            timerLabel.setText(timeLeft + " SECONDS");
            timerProgress.setProgress(timeLeft / 30.0);

            // Change timer color based on remaining time
            if (timeLeft <= 10) {
                timerProgress.setStyle(
                        "-fx-accent: #FF5722; " +
                                "-fx-background-radius: 5; " +
                                "-fx-effect: dropshadow(three-pass-box, rgba(255,87,34,0.3), 5, 0, 0, 1);"
                );
                timerLabel.setTextFill(Color.web("#FF5722"));
                timerIcon.setStyle("-fx-font-size: 20px; -fx-text-fill: #FF5722;");
            }

            if (timeLeft <= 0) {
                countdownTimeline.stop();
                playSound(timeUpSound);

                // Visual feedback for time expiration
                questionLabel.setText("TIME'S UP!");
                questionLabel.setStyle(
                        "-fx-font-size: 32px; " +
                                "-fx-text-fill: #F44336; " +
                                "-fx-font-weight: bold;"
                );

                // Disable all options
                disableAllButtons();

                // Show results after 1.5 second delay
                PauseTransition pause = new PauseTransition(Duration.seconds(1.5));
                pause.setOnFinished(e -> endQuiz());
                pause.play();
            }
        }));
        countdownTimeline.setCycleCount(timeLeft);
        countdownTimeline.play();
    }

    private void checkAnswer(Button selectedButton) {
        if (countdownTimeline != null) countdownTimeline.stop();

        Question current = quizQuestions.get(currentQuestionIndex);
        String selectedOption = getSelectedOption(selectedButton);
        boolean isCorrect = selectedOption.equals(current.answer);

        if (isCorrect) {
            handleCorrectAnswer(selectedButton);
        } else {
            handleWrongAnswer(selectedButton, current);
        }

        disableAllButtons();
        proceedToNextQuestionAfterDelay();
    }

    private void handleCorrectAnswer(Button button) {
        button.setStyle(
                "-fx-background-radius: 15; " +
                        "-fx-background-color: #4CAF50; " +
                        "-fx-text-fill: white; " +
                        "-fx-effect: dropshadow(three-pass-box, rgba(76,175,80,0.5), 20, 0, 0, 0);"
        );

        score++;
        scoreLabel.setText("🏆 SCORE: " + score);
        playSound(correctSound);
    }

    private void handleWrongAnswer(Button button, Question question) {
        button.setStyle(
                "-fx-background-radius: 15; " +
                        "-fx-background-color: #F44336; " +
                        "-fx-text-fill: white; " +
                        "-fx-effect: dropshadow(three-pass-box, rgba(244,67,54,0.5), 20, 0, 0, 0);"
        );

        highlightCorrectAnswer(question);
        playSound(wrongSound);
    }

    private void proceedToNextQuestionAfterDelay() {
        PauseTransition pause = new PauseTransition(Duration.millis(1500));
        pause.setOnFinished(e -> {
            currentQuestionIndex++;
            nextQuestion();
        });
        pause.play();
    }

    private void playSound(AudioClip sound) {
        if (sound != null) {
            sound.play();
        }
    }

    private String getSelectedOption(Button button) {
        for (int i = 0; i < optionButtons.length; i++) {
            if (button == optionButtons[i]) {
                return "option_" + (char)('a' + i);
            }
        }
        return "";
    }

    private void highlightCorrectAnswer(Question q) {
        for (int i = 0; i < optionButtons.length; i++) {
            if (("option_" + (char)('a' + i)).equals(q.answer)) {
                optionButtons[i].setStyle(
                        "-fx-background-radius: 15; " +
                                "-fx-background-color: #4CAF50; " +
                                "-fx-text-fill: white; " +
                                "-fx-effect: dropshadow(three-pass-box, rgba(76,175,80,0.5), 15, 0, 0, 0);"
                );
                break;
            }
        }
    }

    private void resetButtonStyles() {
        String color = "#3F51B5";

        for (Button btn : optionButtons) {
            btn.setStyle(
                    "-fx-background-radius: 15; " +
                            "-fx-background-color: " + color + "; " +
                            "-fx-text-fill: white; " +
                            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 10, 0, 0, 3);"
            );
            btn.setDisable(false);
        }
    }

    private void enableAllButtons() {
        for (Button btn : optionButtons) {
            btn.setDisable(false);
        }
    }

    private void disableAllButtons() {
        for (Button btn : optionButtons) {
            btn.setDisable(true);
        }
    }

    private void endQuiz() {
        // Reset question label style if changed by timeout
        questionLabel.setStyle(
                "-fx-font-size: 22px; " +
                        "-fx-text-fill: #37474f; " +
                        "-fx-font-weight: bold;"
        );

        // Create score screen
        Pane backgroundPane = new Pane();
        double percentage = (double) score / quizQuestions.size();

        // Set performance-based background
        if (percentage >= 0.8) {
            backgroundPane.setStyle(
                    "-fx-background-color: linear-gradient(to bottom right, #e8f5e9, #c8e6c9);"
            );
        } else if (percentage >= 0.6) {
            backgroundPane.setStyle(
                    "-fx-background-color: linear-gradient(to bottom right, #fff8e1, #ffecb3);"
            );
        } else {
            backgroundPane.setStyle(
                    "-fx-background-color: linear-gradient(to bottom right, #ffebee, #ffcdd2);"
            );
        }

        // Main content container
        VBox mainContent = new VBox(20);
        mainContent.setAlignment(Pos.CENTER);
        mainContent.setPadding(new Insets(30));

        // Performance emoji
        Label scoreEmoji = new Label(getPerformanceEmoji());
        scoreEmoji.setStyle("-fx-font-size: 80px;");

        // Results card
        VBox resultsCard = new VBox(15);
        resultsCard.setAlignment(Pos.CENTER);
        resultsCard.setMaxWidth(500);
        resultsCard.setPadding(new Insets(25));
        resultsCard.setStyle(
                "-fx-background-color: white; " +
                        "-fx-background-radius: 15; " +
                        "-fx-border-color: #b0bec5; " +
                        "-fx-border-width: 1; " +
                        "-fx-border-radius: 15; " +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 5);"
        );

        // Score title
        Label scoreTitle = new Label("QUIZ RESULTS");
        scoreTitle.setStyle(
                "-fx-font-size: 28px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-text-fill: #37474f;"
        );

        // Score display
        double percentageScore = (double) score / quizQuestions.size() * 100;
        Label scoreText = new Label(String.format("%d/%d (%.1f%%)",
                score, quizQuestions.size(), percentageScore));
        scoreText.setStyle(
                "-fx-font-size: 32px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-text-fill: #263238;"
        );

        // Performance message
        Label performanceMsg = new Label(getPerformanceMessage(percentageScore));
        performanceMsg.setStyle(
                "-fx-font-size: 18px; " +
                        "-fx-text-fill: #455a64; " +
                        "-fx-font-style: italic; " +
                        "-fx-alignment: center; " +
                        "-fx-wrap-text: true; " +
                        "-fx-padding: 0 20;"
        );
        performanceMsg.setMaxWidth(450);

        resultsCard.getChildren().addAll(scoreTitle, scoreText, performanceMsg);

        // Action buttons
        VBox buttonBox = new VBox(15);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(20, 0, 0, 0));

        Button homeButton = createActionButton("HOME", "#2196F3");
        homeButton.setPrefWidth(220);
        homeButton.setOnAction(e -> {
            // Reset to welcome screen
            scene.setRoot(welcomeLayout);
        });

        Button restartButton = createActionButton("PLAY AGAIN", "#4CAF50");
        restartButton.setPrefWidth(220);
        restartButton.setOnAction(e -> {
            score = 0;
            scoreLabel.setText("🏆 SCORE: " + score);
            PauseTransition delay = new PauseTransition(Duration.seconds(0.5));
            delay.setOnFinished(event -> startQuiz());
            delay.play();
        });

        Button exitButton = createActionButton("EXIT", "#F44336");
        exitButton.setPrefWidth(220);
        exitButton.setOnAction(e -> Platform.exit());

        buttonBox.getChildren().addAll(homeButton, restartButton, exitButton);

        // Assemble all components
        mainContent.getChildren().addAll(scoreEmoji, resultsCard, buttonBox);

        scoreLayout = new StackPane();
        scoreLayout.getChildren().addAll(backgroundPane, mainContent);

        // Switch to score screen
        scene.setRoot(scoreLayout);
    }

    private String getPerformanceEmoji() {
        double percentage = (double) score / quizQuestions.size();
        if (percentage >= 0.9) return "🏆";
        else if (percentage >= 0.8) return "🎉";
        else if (percentage >= 0.7) return "👏";
        else if (percentage >= 0.5) return "👍";
        else return "📖";
    }

    private String getPerformanceMessage(double percentage) {
        if (percentage >= 90) return "Outstanding! You have excellent biblical knowledge!";
        else if (percentage >= 80) return "Great job! You know your Bible well!";
        else if (percentage >= 70) return "Good work! Keep studying the Word!";
        else if (percentage >= 50) return "Not bad! There's room for improvement!";
        else return "Keep studying! The Bible has so much to offer!";
    }

    private Button createActionButton(String text, String color) {
        Button button = new Button(text);
        button.setStyle(
                "-fx-font-size: 18px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-padding: 15 30; " +
                        "-fx-background-radius: 25; " +
                        "-fx-background-color: " + color + "; " +
                        "-fx-text-fill: white; " +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 10, 0, 0, 5);"
        );

        button.setOnMouseEntered(e -> {
            button.setStyle(
                    "-fx-font-size: 18px; " +
                            "-fx-font-weight: bold; " +
                            "-fx-padding: 15 30; " +
                            "-fx-background-radius: 25; " +
                            "-fx-background-color: derive(" + color + ", 20%); " +
                            "-fx-text-fill: white; " +
                            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 15, 0, 0, 8);"
            );
        });

        button.setOnMouseExited(e -> {
            button.setStyle(
                    "-fx-font-size: 18px; " +
                            "-fx-font-weight: bold; " +
                            "-fx-padding: 15 30; " +
                            "-fx-background-radius: 25; " +
                            "-fx-background-color: " + color + "; " +
                            "-fx-text-fill: white; " +
                            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 10, 0, 0, 5);"
            );
        });

        return button;
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        // Style the alert
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle(
                "-fx-background-color: white; " +
                        "-fx-border-color: #2196F3; " +
                        "-fx-border-width: 2px; " +
                        "-fx-border-radius: 10px; " +
                        "-fx-background-radius: 10px;"
        );

        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

class Question {
    public String question;
    public String option_a;
    public String option_b;
    public String option_c;
    public String option_d;
    public String answer;
}