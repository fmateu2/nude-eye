/**
 * Entry point of the Nude Eye optical shop console application.
 * Suppresses verbose API and JSON logging before launching the controller.
 */
import java.util.logging.Level;
import java.util.logging.Logger;
import presentation.Controller;

public class Main {

    /**
     * Application entry point.
     * Silences log output below WARNING level and starts the main controller.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        Logger rootLogger = Logger.getLogger("");
        rootLogger.setLevel(Level.WARNING);
        for (java.util.logging.Handler handler : rootLogger.getHandlers()) {
            handler.setLevel(Level.WARNING);
        }

        Controller controller = new Controller();
        controller.run();
    }
}
