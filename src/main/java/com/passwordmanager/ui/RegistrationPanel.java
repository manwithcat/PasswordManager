package com.passwordmanager.ui;

import com.passwordmanager.database.DatabaseManager;
import com.passwordmanager.crypto.EncryptionManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class RegistrationPanel extends JPanel {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private JButton registerButton;
    private JButton backButton;
    private JLabel errorLabel;
    private JLabel successLabel;
    private PasswordManagerApplication app;
    
    public RegistrationPanel(PasswordManagerApplication app) {
        this.app = app;
        setLayout(new GridBagLayout());
        setBackground(new Color(45, 45, 48));
        initComponents();
    }
    
    private void initComponents() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        JLabel titleLabel = new JLabel("Registration");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        add(titleLabel, gbc);
        
        JLabel usernameLabel = new JLabel("Login:");
        usernameLabel.setForeground(Color.WHITE);
        gbc.gridwidth = 1;
        gbc.gridy = 1;
        add(usernameLabel, gbc);
        
        usernameField = new JTextField(20);
        usernameField.setPreferredSize(new Dimension(250, 35));
        gbc.gridx = 1;
        add(usernameField, gbc);
        
        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setForeground(Color.WHITE);
        gbc.gridx = 0;
        gbc.gridy = 2;
        add(passwordLabel, gbc);
        
        passwordField = new JPasswordField(20);
        passwordField.setPreferredSize(new Dimension(250, 35));
        gbc.gridx = 1;
        add(passwordField, gbc);
        
        JLabel confirmLabel = new JLabel("Confirm:");
        confirmLabel.setForeground(Color.WHITE);
        gbc.gridx = 0;
        gbc.gridy = 3;
        add(confirmLabel, gbc);
        
        confirmPasswordField = new JPasswordField(20);
        confirmPasswordField.setPreferredSize(new Dimension(250, 35));
        gbc.gridx = 1;
        add(confirmPasswordField, gbc);
        
        errorLabel = new JLabel("");
        errorLabel.setForeground(new Color(255, 100, 100));
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        add(errorLabel, gbc);
        
        successLabel = new JLabel("");
        successLabel.setForeground(new Color(100, 255, 100));
        gbc.gridy = 5;
        add(successLabel, gbc);
        
        registerButton = new JButton("Register");
        registerButton.setPreferredSize(new Dimension(150, 40));
        registerButton.addActionListener(this::handleRegister);
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 1;
        add(registerButton, gbc);
        
        backButton = new JButton("Back");
        backButton.setPreferredSize(new Dimension(150, 40));
        backButton.addActionListener(e -> app.showLoginPanel());
        gbc.gridx = 1;
        add(backButton, gbc);
    }
    
    private void handleRegister(ActionEvent e) {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());
        
        errorLabel.setText("");
        successLabel.setText("");
        
        if (username.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Fill in all fields!");
            return;
        }
        
        if (!password.equals(confirmPassword)) {
            errorLabel.setText("Passwords do not match!");
            return;
        }
        
        if (password.length() < 6) {
            errorLabel.setText("Password must be at least 6 characters!");
            return;
        }
        
        String passwordHash = EncryptionManager.encryptMD5(password);
        
        try {
            boolean registered = DatabaseManager.registerUser(username, passwordHash);
            if (registered) {
                successLabel.setText("Registration successful! Go to login.");
                clear();
            } else {
                errorLabel.setText("User with this login already exists!");
            }
        } catch (Exception ex) {
            errorLabel.setText("Error: " + ex.getMessage());
        }
    }
    
    public void clear() {
        usernameField.setText("");
        passwordField.setText("");
        confirmPasswordField.setText("");
        errorLabel.setText("");
        successLabel.setText("");
    }
}
