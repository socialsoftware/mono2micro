package gui;

import collectors.SpoonCollector;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

public class CollectorSettingsWindow extends JFrame {

    private static final String PROJECT_LOCATION_TEXT_FIELD_PLACEHOLDER = "Ex: my/proj/path/src";

    private JTextField projectNameTextField;
    private JTextField projectLocationTextField;
    private int projectFrameworkFieldSelectedOption = Constants.SPRING_DATA_JPA;

    // Unused for now, only one option
    private int dataToCollect = Constants.STRUCTURAL_DATA_COLLECTION_MASK;

    private CollectorSettingsWindow() {
        setTitle("Collector Settings");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        JPanel collectorSettingsPanel = new JPanel();
        collectorSettingsPanel.setLayout(new BoxLayout(collectorSettingsPanel, BoxLayout.Y_AXIS));
        collectorSettingsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        this.buildProjectSettingsSection(collectorSettingsPanel);
        this.buildCollectionSettingsSection(collectorSettingsPanel);
        collectorSettingsPanel.add(Box.createVerticalStrut(20));

        // Submit Button
        JPanel submitPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton submitButton = new JButton("Submit");
        submitButton.addActionListener(e -> onSubmitButtonClicked());
        submitPanel.add(submitButton);

        // Define main layout to compose window
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.add(collectorSettingsPanel, BorderLayout.NORTH);
        mainPanel.add(new JSeparator(SwingConstants.HORIZONTAL), BorderLayout.CENTER);
        mainPanel.add(submitPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
        pack();
    }

    public static void run() {
        SwingUtilities.invokeLater(() -> {
            CollectorSettingsWindow window = new CollectorSettingsWindow();
            window.setVisible(true);
        });
    }

    private void buildProjectSettingsSection(JPanel parentPanel) {
        // Project settings header
        JPanel projectHeaderPanel = this.buildHeaderPanel("Project");
        parentPanel.add(projectHeaderPanel);

        // Project settings name field
        this.buildProjectNameFieldSection(parentPanel);
        // Project settings location field
        this.buildProjectLocationFieldSection(parentPanel);
        // Project settings framework used field options
        this.buildProjectFrameworkFieldSection(parentPanel);
    }

    private JPanel buildHeaderPanel(String headerPanelName) {
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BorderLayout());
        headerPanel.add(new JLabel(headerPanelName), BorderLayout.WEST);
        headerPanel.add(createLineSegment(), BorderLayout.CENTER);
        return headerPanel;
    }

    private void buildProjectNameFieldSection(JPanel parentPanel) {
        JPanel projectNameFieldPanel = new JPanel();
        projectNameFieldPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        projectNameFieldPanel.setBorder(BorderFactory.createEmptyBorder(5, 20, 0, 20));
        projectNameFieldPanel.add(new JLabel("Name:    "));

        projectNameTextField = new JTextField(40);
        projectNameFieldPanel.add(projectNameTextField);

        parentPanel.add(projectNameFieldPanel);
    }

    private void buildProjectLocationFieldSection(JPanel parentPanel) {
        JPanel projectLocationFieldPanel = new JPanel();
        projectLocationFieldPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        projectLocationFieldPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));
        projectLocationFieldPanel.add(new JLabel("Location:"));

        projectLocationTextField = new JTextField(40);
        projectLocationTextField.addFocusListener(this.getProjectLocationFieldFocusListener());
        this.setProjectLocationTextFieldPlaceholder();
        projectLocationFieldPanel.add(projectLocationTextField);

        JButton browseButton = new JButton("Browse");
        browseButton.addActionListener(e -> onBrowseButtonClicked());
        projectLocationFieldPanel.add(browseButton);

        parentPanel.add(projectLocationFieldPanel);
    }

    private void buildProjectFrameworkFieldSection(JPanel parentPanel) {
        JPanel projectFrameworkUsedOptionsPanel = new JPanel();
        projectFrameworkUsedOptionsPanel.setLayout(new BoxLayout(projectFrameworkUsedOptionsPanel, BoxLayout.Y_AXIS));
        projectFrameworkUsedOptionsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 0));

        JRadioButton springDataJpaRadioButton = new JRadioButton("SpringDataJPA", true);
        springDataJpaRadioButton.addActionListener(e -> projectFrameworkFieldSelectedOption = Constants.SPRING_DATA_JPA);
        projectFrameworkUsedOptionsPanel.add(springDataJpaRadioButton);

        JRadioButton fenixFrameworkRadioButton = new JRadioButton("FenixFramework");
        fenixFrameworkRadioButton.addActionListener(e -> projectFrameworkFieldSelectedOption = Constants.FENIX_FRAMEWORK);
        projectFrameworkUsedOptionsPanel.add(fenixFrameworkRadioButton);

        ButtonGroup projectFrameworkUsedRadioButtons = new ButtonGroup();
        projectFrameworkUsedRadioButtons.add(springDataJpaRadioButton);
        projectFrameworkUsedRadioButtons.add(fenixFrameworkRadioButton);

        JPanel projectFrameworkUsedPanel = new JPanel();
        projectFrameworkUsedPanel.setLayout(new BorderLayout());
        projectFrameworkUsedPanel.setBorder(BorderFactory.createEmptyBorder(13, 25, 20, 20));
        projectFrameworkUsedPanel.add(new JLabel("Framework used:"), BorderLayout.NORTH);
        projectFrameworkUsedPanel.add(projectFrameworkUsedOptionsPanel, BorderLayout.CENTER);

        parentPanel.add(projectFrameworkUsedPanel);
    }

    private void buildCollectionSettingsSection(JPanel parentPanel) {
        // Collection settings header
        JPanel collectionHeaderPanel = this.buildHeaderPanel("Collection");
        parentPanel.add(collectionHeaderPanel);

        // Collection data options field
        this.buildCollectionDataOptionsSection(parentPanel);
    }

    private void buildCollectionDataOptionsSection(JPanel parentPanel) {
        JPanel collectionDataOptionsPanel = new JPanel();
        collectionDataOptionsPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        collectionDataOptionsPanel.setBorder(BorderFactory.createEmptyBorder(5, 15, 0, 20));

        JCheckBox collectionStructuralDataCheckBox = new JCheckBox("Structural Data", true);
        collectionStructuralDataCheckBox.addActionListener(this::onStructuralDataCheckBoxClicked);
        collectionDataOptionsPanel.add(collectionStructuralDataCheckBox);

        parentPanel.add(collectionDataOptionsPanel);
    }

    private FocusListener getProjectLocationFieldFocusListener() {
        return new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                if (projectLocationTextField.getText().equals(PROJECT_LOCATION_TEXT_FIELD_PLACEHOLDER)) {
                    projectLocationTextField.setForeground(Color.BLACK);
                    projectLocationTextField.setText("");
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (projectLocationTextField.getText().isEmpty()) {
                    setProjectLocationTextFieldPlaceholder();
                }
            }
        };
    }

    private void setProjectLocationTextFieldPlaceholder() {
        projectLocationTextField.setForeground(Color.LIGHT_GRAY);
        projectLocationTextField.setText(PROJECT_LOCATION_TEXT_FIELD_PLACEHOLDER);
    }

    private void onBrowseButtonClicked() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (fileChooser.showOpenDialog(CollectorSettingsWindow.this) == JFileChooser.APPROVE_OPTION) {
            projectLocationTextField.setForeground(Color.BLACK);
            projectLocationTextField.setText(fileChooser.getSelectedFile().getAbsolutePath());
        }
    }

    private void onStructuralDataCheckBoxClicked(ActionEvent e) {
        dataToCollect = ((JCheckBox) e.getSource()).isSelected() ?
                dataToCollect | Constants.STRUCTURAL_DATA_COLLECTION_MASK :
                dataToCollect ^ Constants.STRUCTURAL_DATA_COLLECTION_MASK;
    }

    private void onSubmitButtonClicked() {
        this.dispose();
        new SpoonCollector(
                projectNameTextField.getText(),
                projectLocationTextField.getText(),
                projectFrameworkFieldSelectedOption)
                .collect().serialize();
    }

    private JPanel createLineSegment() {
        return new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                int y = getHeight() / 2;
                g.setColor(Color.LIGHT_GRAY);
                g.drawLine(10, y, getWidth(), y);
            }
        };
    }
}
