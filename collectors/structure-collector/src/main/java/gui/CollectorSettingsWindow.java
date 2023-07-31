package gui;

import collectors.SpoonCollector;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

public class CollectorSettingsWindow extends JFrame {

    private JTextField nameField;
    private JTextField pathField;
    private JRadioButton jpaRadioButton;
    private JRadioButton ffRadioButton;
    private JCheckBox structuralDataCheckBox;

    private CollectorSettingsWindow() {
        setTitle("Collector Settings");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        // Project Settings Section
        JPanel projectPanel = new JPanel(new BorderLayout());
        projectPanel.add(new JLabel("Project"), BorderLayout.WEST);
        projectPanel.add(createLineSegment(), BorderLayout.CENTER);

        // Project name field
        JPanel projectNamePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        projectNamePanel.setBorder(BorderFactory.createEmptyBorder(5, 20, 0, 20));
        projectNamePanel.add(new JLabel("Name:    "));
        nameField = new JTextField(40);
        projectNamePanel.add(nameField);

        // Project location field
        JPanel projectPathPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        projectPathPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));
        projectPathPanel.add(new JLabel("Location:"));
        pathField = new JTextField(40);
        setPlaceholderText(pathField, "Ex: my/proj/path/src");
        projectPathPanel.add(pathField);

        // Defining single choice buttons
        jpaRadioButton = new JRadioButton("SpringDataJPA");
        ffRadioButton = new JRadioButton("FenixFramework");
        ButtonGroup frameworkGroup = new ButtonGroup();
        frameworkGroup.add(jpaRadioButton);
        frameworkGroup.add(ffRadioButton);

        // Project framework choice field
        JPanel frameworkOptionsPanel = new JPanel();
        frameworkOptionsPanel.setLayout(new BoxLayout(frameworkOptionsPanel, BoxLayout.Y_AXIS));
        frameworkOptionsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 0));
        frameworkOptionsPanel.add(jpaRadioButton);
        frameworkOptionsPanel.add(ffRadioButton);

        JPanel projectFrameworkPanel = new JPanel(new BorderLayout());
        projectFrameworkPanel.setBorder(BorderFactory.createEmptyBorder(13, 25, 20, 20));
        projectFrameworkPanel.add(new JLabel("Framework used:"), BorderLayout.NORTH);
        projectFrameworkPanel.add(frameworkOptionsPanel, BorderLayout.CENTER);

        // Collector Settings Section
        JPanel collectorPanel = new JPanel(new BorderLayout());
        collectorPanel.add(new JLabel("Collection"), BorderLayout.WEST);
        collectorPanel.add(createLineSegment(), BorderLayout.CENTER);

        structuralDataCheckBox = new JCheckBox("Structural Data");
        JPanel dataCollectionPanel = new JPanel();
        //dataCollectionPanel.setLayout(new BoxLayout(dataCollectionPanel, BoxLayout.Y_AXIS));
        dataCollectionPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        dataCollectionPanel.setBorder(BorderFactory.createEmptyBorder(5, 15, 0, 20));
        dataCollectionPanel.add(structuralDataCheckBox);

        // Submit Button
        JPanel submitPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton submitButton = new JButton("Submit");
        submitButton.addActionListener(e -> onSubmitButtonClicked());
        submitPanel.add(submitButton);

        // Add all sections to a layout
        JPanel settingsPanel = new JPanel();
        settingsPanel.setLayout(new BoxLayout(settingsPanel, BoxLayout.Y_AXIS));
        settingsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        settingsPanel.add(projectPanel);
        settingsPanel.add(projectNamePanel);
        settingsPanel.add(projectPathPanel);
        settingsPanel.add(projectFrameworkPanel);
        settingsPanel.add(collectorPanel);
        settingsPanel.add(dataCollectionPanel);
        settingsPanel.add(Box.createVerticalStrut(20));

        // Define main layout to compose window
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.add(settingsPanel, BorderLayout.NORTH);
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

    private void onSubmitButtonClicked() {
        this.dispose();
        new SpoonCollector(nameField.getText(), pathField.getText(), jpaRadioButton.isSelected() ? 0 : 1)
                .collect()
                .serialize();
    }

    private void setPlaceholderText(JTextField textField, String placeholder) {
        textField.setText(placeholder);
        textField.setForeground(Color.LIGHT_GRAY);

        textField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                if (textField.getText().equals(placeholder)) {
                    textField.setText("");
                    textField.setForeground(Color.BLACK);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (textField.getText().isEmpty()) {
                    textField.setText(placeholder);
                    textField.setForeground(Color.LIGHT_GRAY);
                }
            }
        });
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







