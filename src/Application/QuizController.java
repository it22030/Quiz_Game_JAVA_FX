package Application;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.Duration;

import java.sql.*;
import java.util.*;

public class QuizController {

    @FXML private Label questionLabel, scoreLabel, timerLabel;
    @FXML private RadioButton option1, option2, option3, option4;
    @FXML private TextField playerNameField;
    @FXML private ToggleGroup optionsGroup;

    private List<Question> questionList;
    private int currentQuestion = 0;
    private int score = 0;
    private Timeline timeline;
    private int timeLeft = 30;

    @FXML
    public void initialize() {
        // Safety check (only necessary if you still suspect FXML not loading properly)
        if (optionsGroup == null) {
            optionsGroup = new ToggleGroup();
            option1.setToggleGroup(optionsGroup);
            option2.setToggleGroup(optionsGroup);
            option3.setToggleGroup(optionsGroup);
            option4.setToggleGroup(optionsGroup);
        }

        loadQuestions();
        startTimer();
        showQuestion();
    }

    private void loadQuestions() {
        questionList = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM questions ORDER BY RAND() LIMIT 5")) {

            while (rs.next()) {
                String q = rs.getString("question");
                String[] opts = {
                        rs.getString("option1"),
                        rs.getString("option2"),
                        rs.getString("option3"),
                        rs.getString("option4")
                };
                int correct = rs.getInt("correct_option");
                questionList.add(new Question(q, opts, correct));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void showQuestion() {
        if (currentQuestion < questionList.size()) {
            Question q = questionList.get(currentQuestion);
            questionLabel.setText(q.getQuestion());
            option1.setText(q.getOptions()[0]);
            option2.setText(q.getOptions()[1]);
            option3.setText(q.getOptions()[2]);
            option4.setText(q.getOptions()[3]);
            optionsGroup.selectToggle(null);
        } else {
            endGame();
        }
    }

    @FXML
    private void nextQuestion() {
        RadioButton selected = (RadioButton) optionsGroup.getSelectedToggle();
        if (selected != null) {
            int selectedIndex = 1;
            if (selected == option2) selectedIndex = 2;
            else if (selected == option3) selectedIndex = 3;
            else if (selected == option4) selectedIndex = 4;

            if (selectedIndex == questionList.get(currentQuestion).getCorrectOption()) {
                score++;
            }

            currentQuestion++;
            timeLeft = 30;
            showQuestion();
        } else {
            scoreLabel.setText("Please select an option.");
        }
    }

    @FXML
    private void restartGame() {
        score = 0;
        currentQuestion = 0;
        timeLeft = 30;
        scoreLabel.setText("");
        playerNameField.setDisable(false);
        loadQuestions();
        showQuestion();
        startTimer();
    }

    @FXML
    private void exitGame() {
        System.exit(0);
    }

    private void endGame() {
        stopTimer();
        questionLabel.setText("Game Over!");
        option1.setVisible(false);
        option2.setVisible(false);
        option3.setVisible(false);
        option4.setVisible(false);
        scoreLabel.setText("Score: " + score);

        saveScore(playerNameField.getText(), score);
    }

    private void saveScore(String name, int score) {
        if (name == null || name.isEmpty()) return;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("INSERT INTO scores (player_name, score) VALUES (?, ?)")) {

            ps.setString(1, name);
            ps.setInt(2, score);
            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void startTimer() {
        stopTimer();
        timeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            timerLabel.setText("Time Left: " + timeLeft);
            if (timeLeft <= 0) {
                currentQuestion++;
                timeLeft = 30;
                showQuestion();
            }
            timeLeft--;
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private void stopTimer() {
        if (timeline != null) {
            timeline.stop();
        }
    }
}
