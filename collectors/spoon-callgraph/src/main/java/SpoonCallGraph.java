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

    static String repoURL = null;
    static int selectedIndex = -1;

    public static void main(String[] args) throws GitAPIException, IOException {
        showDialog();

        File file = new File(System.getProperty("user.dir") + "/tmpRepository");
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
        String repoName = split1[0];

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

        FileUtils.deleteDirectory(file);
    }

    private static void showDialog() {
        JFrame frame = new JFrame();
        frame.setUndecorated(true);
        frame.setVisible(true);
        frame.setLocationRelativeTo(null);

        JPanel pane = new JPanel();
        pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));

        JTextField repoField = new JTextField(5);

        String[] choices = {"FenixFramework", "SpringDataJPA"};
        JComboBox<String> dropDownField = new JComboBox<>(choices);

        pane.add(new JLabel("Git repository clone link:"));
        pane.add(repoField);

        pane.add(new JLabel("ORM used:"));
        pane.add(dropDownField);

        int option = JOptionPane.showConfirmDialog(frame, pane, "Parser Settings", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE);

        if (option == JOptionPane.OK_OPTION) {
            repoURL = repoField.getText();
            selectedIndex = dropDownField.getSelectedIndex();
        }
        else {
            System.exit(1);
        }
        frame.dispose();
    }
}
