package com.passwordmanager.ui;

import com.formdev.flatlaf.FlatDarculaLaf;
import com.passwordmanager.database.DatabaseManager;

import javax.swing.*;
import java.awt.*;

public class PasswordManagerApplication extends JFrame {
    private CardLayout cardLayout;
    private JPanel mainPanel;
    private LoginPanel loginPanel;
    private RegistrationPanel registrationPanel;
    private MainPanel mainPanel_logged;
    
    public PasswordManagerApplication() {
        DatabaseManager.initialize();
        
        setTitle("Password Manager");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
        setResizable(false);
        
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        
        loginPanel = new LoginPanel(this);
        registrationPanel = new RegistrationPanel(this);
        mainPanel_logged = new MainPanel(this);
        
        mainPanel.add(loginPanel, "LOGIN");
        mainPanel.add(registrationPanel, "REGISTER");
        mainPanel.add(mainPanel_logged, "MAIN");
        
        add(mainPanel);
        
        cardLayout.show(mainPanel, "LOGIN");
        
        setVisible(true);
    }
    
    public void showLoginPanel() {
        loginPanel.clear();
        cardLayout.show(mainPanel, "LOGIN");
    }
    
    public void showRegistrationPanel() {
        registrationPanel.clear();
        cardLayout.show(mainPanel, "REGISTER");
    }
    
    public void showMainPanel(int userId) {
        mainPanel_logged.setUserId(userId);
        mainPanel_logged.loadPasswords();
        cardLayout.show(mainPanel, "MAIN");
    }
    
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new FlatDarculaLaf());
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }
        
        SwingUtilities.invokeLater(PasswordManagerApplication::new);
    }
}