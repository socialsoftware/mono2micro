import collectors.FenixFrameworkCollector;
import collectors.SpoonCollector;
import collectors.SpringDataJPACollector;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;

import javax.swing.*;
import java.io.File;
import java.io.IOException;

public class SpoonCallGraph {

    static String localPath = null;
    static String repoURL = null;
    static int selectedIndex = -1;
    private static String projectName;

    public static void main(String[] args) throws GitAPIException, IOException {
        showDialog();

        File file;
        String repoName;
        boolean localAnalysis;

        if (!localPath.isEmpty()) {
            localAnalysis = true;
            file = new File(localPath.trim());
            repoName = projectName;
        }
        else {
            localAnalysis = false;
            file = new File(System.getProperty("user.dir") + "/tmpRepository");
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
            repoName = split1[0];
        }

        SpoonCollector collector = null;
        switch (selectedIndex) {
            case 0:
                collector = new FenixFrameworkCollector(file.getAbsolutePath(), repoName);
                break;
            case 1:
                collector = new SpringDataJPACollector(file.getAbsolutePath(), repoName);
                break;
            default:
                System.exit(1);
        }

        collector.run();

        if (!localAnalysis)
            FileUtils.deleteDirectory(file);
    }

    private static void showDialog() {
        JFrame frame = new JFrame();
        frame.setUndecorated(true);
        frame.setVisible(true);
        frame.setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JTextField projectNameField = new JTextField(5);
        JTextField localField = new JTextField(5);
        JTextField repoField = new JTextField(5);

        String[] choices = {"FenixFramework", "SpringDataJPA"};
        JComboBox<String> dropDownField = new JComboBox<>(choices);

        panel.add(new JLabel("Project name:"));
        panel.add(projectNameField);

        panel.add(new JLabel("Project local path:"));
        panel.add(localField);

        panel.add(new JLabel(""));
        panel.add(new JLabel("OR"));
        panel.add(new JLabel(""));

        panel.add(new JLabel("Git repository clone link:"));
        panel.add(repoField);

        panel.add(new JLabel("ORM used:"));
        panel.add(dropDownField);


        JLabel note = new JLabel();
        String s = "Note: Make sure that the provided sources for FenixFramework projects include " +
                "the dml generated resources classes (_Base).";
        note.setText("<html>"+ s +"</html>");
        panel.add(note);

        int option = JOptionPane.showConfirmDialog(frame, panel, "Parser Settings", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE);

        if (option == JOptionPane.OK_OPTION) {
            projectName = projectNameField.getText();
            localPath = localField.getText();
            repoURL = repoField.getText();
            selectedIndex = dropDownField.getSelectedIndex();
        }
        else {
            System.exit(1);
        }
        frame.dispose();
    }
}
