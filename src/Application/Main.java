package Application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Application/quiz.fxml"));
            Scene scene = new Scene(loader.load());
            primaryStage.setScene(scene);
            primaryStage.setTitle("JavaFX Quiz Game");
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace(); // Show error in console
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
