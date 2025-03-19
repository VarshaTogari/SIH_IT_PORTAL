import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.sql.*;

public class SIHITPortal extends JFrame implements ActionListener {
    private JTextField studentNameField;
    private JTextField emailField;
    private JTextField ideaTitleField;
    private JTextArea ideaDescriptionArea;
    private JTextField categoryField;
    private JComboBox<String> departmentComboBox;
    private JTextArea resultArea;
    private Image backgroundImage;

    private Connection connection;
    private static final String URL = "jdbc:mysql://localhost:3306/smart_india_hackathon";
    private static final String USER = "root";
    private static final String PASSWORD = "Maranatha#1";

    public SIHITPortal() {
        super("SIH IT Portal");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        try {
            backgroundImage = ImageIO.read(new File("C:\\Users\\togar\\OneDrive\\Desktop\\dbms.jpg"));
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Failed to load background image.", "Error", JOptionPane.ERROR_MESSAGE);
        }

        BackgroundPanel panel = new BackgroundPanel();
        panel.setLayout(new GridLayout(7, 2, 10, 10));

        Font headingFont = new Font("Arial", Font.BOLD, 20);
        Font textFont = new Font("Arial", Font.PLAIN, 16);
        studentNameField = new JTextField(20);
        studentNameField.setFont(textFont);
        emailField = new JTextField(20);
        emailField.setFont(textFont);
        ideaTitleField = new JTextField(20);
        ideaTitleField.setFont(textFont);
        ideaDescriptionArea = new JTextArea(5, 20);
        ideaDescriptionArea.setFont(textFont);
        JLabel categoryLabel = new JLabel("Category:");
        categoryLabel.setFont(headingFont);
        categoryField = new JTextField(20);
        categoryField.setFont(textFont);
        JLabel departmentLabel = new JLabel("Department:");
        departmentLabel.setFont(headingFont);
        departmentComboBox = new JComboBox<>(new String[]{"CSE", "IT"});
        departmentComboBox.setFont(textFont);
        JButton submitButton = new JButton("Submit Idea");
        submitButton.setFont(textFont);

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            if (connection != null) {
                populateDepartmentComboBox();
            } else {
                throw new SQLException("Failed to establish connection.");
            }
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Failed to establish database connection.", "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        resultArea = new JTextArea(10, 50);
        resultArea.setEditable(false);
        resultArea.setFont(textFont);
        JScrollPane scrollPane = new JScrollPane(resultArea);

        panel.add(new JLabel("Student Name:"));
        panel.add(studentNameField);
        panel.add(new JLabel("Email:"));
        panel.add(emailField);
        panel.add(new JLabel("Idea Title:"));
        panel.add(ideaTitleField);
        panel.add(new JLabel("Idea Description:"));
        panel.add(new JScrollPane(ideaDescriptionArea));
        panel.add(categoryLabel);
        panel.add(categoryField);
        panel.add(departmentLabel);
        panel.add(departmentComboBox);
        panel.add(submitButton);
        panel.add(scrollPane);

        add(panel);
        submitButton.addActionListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("Submit Idea")) {
            String studentName = studentNameField.getText();
            String email = emailField.getText();
            String ideaTitle = ideaTitleField.getText();
            String ideaDescription = ideaDescriptionArea.getText();
            String category = categoryField.getText();
            Object selectedDepartment = departmentComboBox.getSelectedItem();

            if (selectedDepartment == null) {
                resultArea.setText("Please select a department.");
                return;
            }

            String department = selectedDepartment.toString();

            if (!studentName.isEmpty() && !email.isEmpty() && !ideaTitle.isEmpty() && !ideaDescription.isEmpty() && !category.isEmpty() && !department.isEmpty()) {
                submitIdea(studentName, email, ideaTitle, ideaDescription, category, department);
            } else {
                resultArea.setText("Please fill out all fields.");
            }
        }
    }

    private void populateDepartmentComboBox() {
        try {
            String query = "SELECT DeptName FROM Department";
            PreparedStatement stmt = connection.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                departmentComboBox.addItem(rs.getString("DeptName"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void submitIdea(String studentName, String email, String ideaTitle, String ideaDescription, String category, String department) {
        try {
            String getUserIDQuery = "SELECT UserID FROM User WHERE Email=?";
            PreparedStatement getUserIDStmt = connection.prepareStatement(getUserIDQuery);
            getUserIDStmt.setString(1, email);
            ResultSet userIDResult = getUserIDStmt.executeQuery();
            if (!userIDResult.next()) {
                resultArea.setText("User with email " + email + " not found.");
                return;
            }
            int userID = userIDResult.getInt("UserID");

            String insertIdeaQuery = "INSERT INTO Idea (Title, Description, Category, SubmissionDate, Status, UserID) VALUES (?, ?, ?, NOW(), 'pending', ?)";
            PreparedStatement insertIdeaStmt = connection.prepareStatement(insertIdeaQuery);
            insertIdeaStmt.setString(1, ideaTitle);
            insertIdeaStmt.setString(2, ideaDescription);
            insertIdeaStmt.setString(3, category);
            insertIdeaStmt.setInt(4, userID);
            int rowsAffected = insertIdeaStmt.executeUpdate();
            if (rowsAffected > 0) {
                resultArea.setText("Idea submitted successfully.");
            } else {
                resultArea.setText("Failed to submit idea.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            resultArea.setText("Error: " + e.getMessage());
        }
    }

    class BackgroundPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (backgroundImage != null) {
                g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            SIHITPortal portal = new SIHITPortal();
            portal.setVisible(true);
        });
    }
}
