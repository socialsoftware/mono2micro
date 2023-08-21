import collectors.SpoonCollector;
import gui.CollectorSettingsWindow;
import gui.Constants;

public class Main {

    public static void main(String[] args) {
        if (args.length == 0) { // Launch GUI
            CollectorSettingsWindow.run();
        } else if (args.length == 4) {
            runCollector(args[0], args[1], Integer.parseInt(args[2]));
        } else {
            System.err.println("ERROR : Invalid program start arguments");
            System.exit(1);
        }
    }

    public static void runCollector(String projectName, String sourcesPath, int projectFramework) {
        new SpoonCollector(projectName, sourcesPath, projectFramework).collect().serialize();
    }
}
