package com.passwordmanager.ui;

import com.passwordmanager.database.DatabaseManager;
import com.passwordmanager.crypto.EncryptionManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class PasswordDialog extends JDialog {
    private JTextField serviceNameField;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JComboBox<EncryptionManager.EncryptionType> encryptionTypeCombo;
    private JButton saveButton;
    private JButton cancelButton;
    private JLabel infoLabel;
    
    private int userId;
    private DatabaseManager.PasswordEntry editingEntry;
    private MainPanel mainPanel;
    
    public PasswordDialog(MainPanel mainPanel, int userId, DatabaseManager.PasswordEntry entry) {
        super((Frame) SwingUtilities.getWindowAncestor(mainPanel), 
                entry == null ? "Add Password" : "Edit Password", true);
        
        this.mainPanel = mainPanel;
        this.userId = userId;
        this.editingEntry = entry;
        
        setSize(400, 300);
        setLocationRelativeTo((Frame) SwingUtilities.getWindowAncestor(mainPanel));
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        
        initComponents();
        
        if (entry != null) {
            loadData(entry);
        }
    }
    
    private void initComponents() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        JLabel serviceLabel = new JLabel("Service Name:");
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.3;
        panel.add(serviceLabel, gbc);
        
        serviceNameField = new JTextField(20);
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        panel.add(serviceNameField, gbc);
        
        JLabel usernameLabel = new JLabel("Login:");
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.3;
        panel.add(usernameLabel, gbc);
        
        usernameField = new JTextField(20);
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        panel.add(usernameField, gbc);
        
        JLabel passwordLabel = new JLabel("Password:");
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0.3;
        panel.add(passwordLabel, gbc);
        
        passwordField = new JPasswordField(20);
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        panel.add(passwordField, gbc);
        
        JLabel encryptionLabel = new JLabel("Encryption:");
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0.3;
        panel.add(encryptionLabel, gbc);
        
        encryptionTypeCombo = new JComboBox<>(EncryptionManager.EncryptionType.values());
        encryptionTypeCombo.setSelectedItem(EncryptionManager.EncryptionType.BASE64);
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        panel.add(encryptionTypeCombo, gbc);
        
        infoLabel = new JLabel("<html>Encryption methods:<br>" +
                "<b>PLAINTEXT</b> - no encryption<br>" +
                "<b>BASE64</b> - Base64 encoding<br>" +
                "<b>MD5</b> - hashing (irreversible)<br>" +
                "<b>AES_WITH_SALT</b> - AES with salt<br>" +
                "<b>FEISTEL</b> - Feistel cipher</html>");
        infoLabel.setFont(new Font("Arial", Font.PLAIN, 10));
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        panel.add(infoLabel, gbc);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        saveButton = new JButton("Save");
        saveButton.addActionListener(this::handleSave);
        buttonPanel.add(saveButton);
        
        cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> dispose());
        buttonPanel.add(cancelButton);
        
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        panel.add(buttonPanel, gbc);
        
        add(panel);
    }
    
    private void loadData(DatabaseManager.PasswordEntry entry) {
        serviceNameField.setText(entry.serviceName);
        usernameField.setText(entry.username);
        
        try {
            EncryptionManager.EncryptionType type = 
                    EncryptionManager.EncryptionType.valueOf(entry.encryptionType);
            if (EncryptionManager.isReversible(type)) {
                String decrypted = EncryptionManager.decrypt(entry.password, type);
                passwordField.setText(decrypted);
            } else {
                passwordField.setText("[Cannot be displayed - " + entry.encryptionType + "]");
            }
            encryptionTypeCombo.setSelectedItem(type);
        } catch (Exception ex) {
            passwordField.setText("[Decryption error]");
        }
    }
    
    private void handleSave(ActionEvent e) {
        String serviceName = serviceNameField.getText().trim();
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        EncryptionManager.EncryptionType encryptionType = 
                (EncryptionManager.EncryptionType) encryptionTypeCombo.getSelectedItem();
        
        if (serviceName.isEmpty() || username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Fill in all fields!");
            return;
        }
        
        try {
            String encryptedPassword = EncryptionManager.encrypt(password, encryptionType);
            
            if (editingEntry == null) {
                DatabaseManager.savePassword(userId, serviceName, username, 
                        encryptedPassword, encryptionType.name());
            } else {
                DatabaseManager.updatePassword(editingEntry.id, encryptedPassword, 
                        encryptionType.name());
            }
            
            mainPanel.loadPasswords();
            JOptionPane.showMessageDialog(this, "Password saved!");
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }
}
