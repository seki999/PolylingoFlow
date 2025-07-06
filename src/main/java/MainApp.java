import com.polylingoflow.ui.MainUI;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * The main entry point for the PolylingoFlow application.
 * This class is responsible for launching the JavaFX application.
 */
public class MainApp extends Application {

    /**
     * The main entry point for all JavaFX applications.
     * The start method is called after the init method has returned,
     * and after the system is ready for the application to begin running.
     *
     * @param primaryStage the primary stage for this application, onto which
     *                     the application scene can be set.
     */
    @Override
    public void start(Stage primaryStage) {
        new MainUI().start(primaryStage);
    }

    public static void main(String[] args) {
        launch(args);
    }
}