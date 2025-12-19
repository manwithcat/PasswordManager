package com.passwordmanager.ui;

import com.passwordmanager.database.DatabaseManager;
import com.passwordmanager.crypto.EncryptionManager;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class MainPanel extends JPanel {
    private int currentUserId;
    private JTable passwordsTable;
    private DefaultTableModel tableModel;
    private PasswordManagerApplication app;
    private JButton addButton;
    private JButton editButton;
    private JButton deleteButton;
    private JButton logoutButton;
    private JButton showPasswordButton;
    private JLabel userLabel;
    
    public MainPanel(PasswordManagerApplication app) {
        this.app = app;
        setLayout(new BorderLayout());
        setBackground(new Color(45, 45, 48));
        initComponents();
    }
    
    private void initComponents() {
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(37, 37, 38));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        userLabel = new JLabel();
        userLabel.setForeground(Color.WHITE);
        userLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        topPanel.add(userLabel, BorderLayout.WEST);
        
        logoutButton = new JButton("Logout");
        logoutButton.addActionListener(e -> handleLogout());
        topPanel.add(logoutButton, BorderLayout.EAST);
        
        add(topPanel, BorderLayout.NORTH);
        
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(new Color(45, 45, 48));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        String[] columnNames = {"Service", "Login", "Password", "Encryption", "ID"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        passwordsTable = new JTable(tableModel);
        passwordsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        passwordsTable.getColumnModel().getColumn(4).setMaxWidth(0);
        passwordsTable.getColumnModel().getColumn(4).setMinWidth(0);
        passwordsTable.getColumnModel().getColumn(4).setPreferredWidth(0);
        
        JScrollPane scrollPane = new JScrollPane(passwordsTable);
        centerPanel.add(scrollPane, BorderLayout.CENTER);
        
        add(centerPanel, BorderLayout.CENTER);
        
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        bottomPanel.setBackground(new Color(37, 37, 38));
        
        addButton = new JButton("Add Password");
        addButton.addActionListener(e -> showAddPasswordDialog());
        bottomPanel.add(addButton);
        
        showPasswordButton = new JButton("Show Password");
        showPasswordButton.addActionListener(e -> showPassword());
        bottomPanel.add(showPasswordButton);
        
        editButton = new JButton("Edit");
        editButton.addActionListener(e -> showEditPasswordDialog());
        bottomPanel.add(editButton);
        
        deleteButton = new JButton("Delete");
        deleteButton.addActionListener(e -> deletePassword());
        bottomPanel.add(deleteButton);
        
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    public void setUserId(int userId) {
        this.currentUserId = userId;
        userLabel.setText("User ID: " + userId);
    }
    
    public void loadPasswords() {
        tableModel.setRowCount(0);
        try {
            List<DatabaseManager.PasswordEntry> passwords = DatabaseManager.getUserPasswords(currentUserId);
            for (DatabaseManager.PasswordEntry entry : passwords) {
                Object[] row = {
                        entry.serviceName,
                        entry.username,
                        "***",
                        entry.encryptionType,
                        entry.id
                };
                tableModel.addRow(row);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error loading passwords: " + ex.getMessage());
        }
    }
    
    private void showPassword() {
        int selectedRow = passwordsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Select a password!");
            return;
        }
        
        try {
            int passwordId = (Integer) tableModel.getValueAt(selectedRow, 4);
            List<DatabaseManager.PasswordEntry> passwords = DatabaseManager.getUserPasswords(currentUserId);
            
            for (DatabaseManager.PasswordEntry entry : passwords) {
                if (entry.id == passwordId) {
                    EncryptionManager.EncryptionType type = 
                            EncryptionManager.EncryptionType.valueOf(entry.encryptionType);
                    
                    String decryptedPassword;
                    if (EncryptionManager.isReversible(type)) {
                        decryptedPassword = EncryptionManager.decrypt(entry.password, type);
                    } else {
                        decryptedPassword = "[Cannot be decrypted - " + type.getDisplayName() + "]";
                    }
                    
                    JOptionPane.showMessageDialog(this, 
                            "Service: " + entry.serviceName + "\n" +
                            "Login: " + entry.username + "\n" +
                            "Password: " + decryptedPassword + "\n" +
                            "Encryption: " + entry.encryptionType,
                            "Password Info",
                            JOptionPane.INFORMATION_MESSAGE);
                    break;
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }
    
    private void showAddPasswordDialog() {
        new PasswordDialog(this, currentUserId, null).setVisible(true);
    }
    
    private void showEditPasswordDialog() {
        int selectedRow = passwordsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Select a password to edit!");
            return;
        }
        
        int passwordId = (Integer) tableModel.getValueAt(selectedRow, 4);
        try {
            List<DatabaseManager.PasswordEntry> passwords = DatabaseManager.getUserPasswords(currentUserId);
            for (DatabaseManager.PasswordEntry entry : passwords) {
                if (entry.id == passwordId) {
                    new PasswordDialog(this, currentUserId, entry).setVisible(true);
                    break;
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }
    
    private void deletePassword() {
        int selectedRow = passwordsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Select a password to delete!");
            return;
        }
        
        int result = JOptionPane.showConfirmDialog(this, "Are you sure?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (result == JOptionPane.YES_OPTION) {
            try {
                int passwordId = (Integer) tableModel.getValueAt(selectedRow, 4);
                DatabaseManager.deletePassword(passwordId);
                loadPasswords();
                JOptionPane.showMessageDialog(this, "Password deleted!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        }
    }
    
    private void handleLogout() {
        app.showLoginPanel();
    }
}
