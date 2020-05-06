import collectors.FenixFrameworkCollector;
import collectors.SpoonCollector;
import collectors.SpringDataJPACollector;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import util.Constants;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Scanner;

public class SpoonCallGraph {
    private static int launcherChoice = -1;
    private static int sourcesChoice = -1;
    private static int ormChoice = -1;

    private static String sourcesPath = null;
    private static String repoURL = null;
    private static String projectNameInput;

    public static void main(String[] args) throws GitAPIException, IOException {
        if (args.length == 0) {
            showDialogFirst();
            showDialogSecond();
        }
        else if (args.length == 5) {
            // Launcher Choice, Sources Choice, ORM Choice, projectName, path/link
            launcherChoice = Integer.parseInt(args[0]);
            sourcesChoice = Integer.parseInt(args[1]);
            ormChoice = Integer.parseInt(args[2]);
            projectNameInput = args[3];
            sourcesPath = args[4];
        }
        else {
            System.err.println("Invalid args");
            System.exit(1);
        }

        File file;
        String projectName;

        if (sourcesChoice == Constants.LOCAL) {
            file = new File(sourcesPath.trim());
            projectName = projectNameInput;
        }
        else { // GITHUB
            file = new File(System.getProperty("user.dir") + "/tmpRepositoryClone");
            if (file.exists()) {
                FileUtils.deleteDirectory(file);
            }

            System.out.println("Cloning repository...");
            // clone repo to tmp folder
            Git.cloneRepository()
                    .setURI(repoURL)
                    .setDirectory(file)
                    .call();

            String[] split = repoURL.split("/");
            String[] split1 = split[split.length - 1].split("\\.");
            projectName = split1[0];
        }

        SpoonCollector collector = null;
        switch (ormChoice) {
            case Constants.FENIX_FRAMEWORK:
                collector = new FenixFrameworkCollector(launcherChoice, projectName, file.getAbsolutePath());
                break;
            case Constants.SPRING_DATA_JPA:
                collector = new SpringDataJPACollector(launcherChoice, projectName, file.getAbsolutePath());
                break;
            default:
                System.exit(1);
        }

        collector.run();


        if (sourcesChoice == Constants.GITHUB) {
            Scanner scanner = new Scanner(System.in);
            System.out.println("Do you want to delete the cloned repository? (\'YES\', \'NO\')");
            String input = scanner.nextLine();
            if (input.equals("YES"))
                FileUtils.deleteDirectory(file);
        }
    }

    private static void showDialogSecond() {
        JFrame frame = new JFrame();
        frame.setUndecorated(true);
        frame.setVisible(true);
        frame.setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JTextField projectNameField = new JTextField(5);
        JTextField pathField = new JTextField(5);

        panel.add(new JLabel("Project name:"));
        panel.add(projectNameField);

        String sourcesText = "";
        if (sourcesChoice == Constants.LOCAL && launcherChoice != Constants.JAR_LAUNCHER)
            sourcesText = "Project folder path:";
        if (sourcesChoice == Constants.LOCAL && launcherChoice == Constants.JAR_LAUNCHER)
            sourcesText = "Project folder path (with packages inside):";
        if (sourcesChoice == Constants.GITHUB)
            sourcesText = "GitHub clone link:";

        panel.add(new JLabel(sourcesText));
        panel.add(pathField);

        String[] frameworkChoices = {"FenixFramework", "SpringDataJPA"};
        JComboBox<String> frameworkDropdown = new JComboBox<>(frameworkChoices);

        panel.add(new JLabel("ORM used:"));
        panel.add(frameworkDropdown);

        JLabel note = new JLabel();
        String s = "Note: Make sure that the provided sources for FenixFramework projects include " +
                "the dml generated resources classes (_Base).";
        note.setText("<html>"+ s +"</html>");
        panel.add(note);

        int option = JOptionPane.showConfirmDialog(
                frame,
                panel,
                Constants.launcherIntToName.get(launcherChoice) + " Settings",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.INFORMATION_MESSAGE
        );

        if (option == JOptionPane.OK_OPTION) {
            projectNameInput = projectNameField.getText();
            sourcesPath = pathField.getText();
            ormChoice = frameworkDropdown.getSelectedIndex();
        }
        else {
            System.exit(1);
        }
        frame.dispose();
    }

    private static void showDialogFirst() {
        JFrame frame = new JFrame();
        frame.setUndecorated(true);
        frame.setVisible(true);
        frame.setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        String[] launcherChoices = {Constants.launcherIntToName.get(0), Constants.launcherIntToName.get(1), Constants.launcherIntToName.get(2)};
        JComboBox<String> launcherDropdown = new JComboBox<>(launcherChoices);

        String[] sourceChoices = {"Local", "GitHub"};
        JComboBox<String> sourcesDropdown = new JComboBox<>(sourceChoices);

        panel.add(new JLabel("Launcher to use:"));
        panel.add(launcherDropdown);

        panel.add(new JLabel("Sources location:"));
        panel.add(sourcesDropdown);

        int option = JOptionPane.showConfirmDialog(frame, panel, "Parser Settings", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE);

        if (option == JOptionPane.OK_OPTION) {
            launcherChoice = launcherDropdown.getSelectedIndex();
            sourcesChoice = sourcesDropdown.getSelectedIndex();
        }
        else {
            System.exit(1);
        }
        frame.dispose();
    }
}
