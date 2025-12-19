package com.passwordmanager.ui;

import com.passwordmanager.database.DatabaseManager;
import com.passwordmanager.crypto.EncryptionManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class LoginPanel extends JPanel {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton registerButton;
    private JLabel errorLabel;
    private PasswordManagerApplication app;
    
    public LoginPanel(PasswordManagerApplication app) {
        this.app = app;
        setLayout(new GridBagLayout());
        setBackground(new Color(45, 45, 48));
        initComponents();
    }
    
    private void initComponents() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        JLabel titleLabel = new JLabel("Password Manager");
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
        
        errorLabel = new JLabel("");
        errorLabel.setForeground(new Color(255, 100, 100));
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        add(errorLabel, gbc);
        
        loginButton = new JButton("Sign In");
        loginButton.setPreferredSize(new Dimension(100, 40));
        loginButton.addActionListener(this::handleLogin);
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 1;
        add(loginButton, gbc);
        
        registerButton = new JButton("Register");
        registerButton.setPreferredSize(new Dimension(100, 40));
        registerButton.addActionListener(e -> app.showRegistrationPanel());
        gbc.gridx = 1;
        add(registerButton, gbc);
    }
    
    private void handleLogin(ActionEvent e) {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        
        if (username.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Fill in all fields!");
            return;
        }
        
        String passwordHash = EncryptionManager.encryptMD5(password);
        
        try {
            Integer userId = DatabaseManager.authenticateUser(username, passwordHash);
            if (userId != null) {
                errorLabel.setText("");
                app.showMainPanel(userId);
            } else {
                errorLabel.setText("Invalid username or password!");
            }
        } catch (Exception ex) {
            errorLabel.setText("Error: " + ex.getMessage());
        }
    }
    
    public void clear() {
        usernameField.setText("");
        passwordField.setText("");
        errorLabel.setText("");
    }
}
