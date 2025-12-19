package com.passwordmanager.database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {
    private static final String DATABASE_URL = "jdbc:sqlite:password_manager.db";
    private static final String TABLE_USERS = "users";
    private static final String TABLE_PASSWORDS = "passwords";
    
    static {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("SQLite JDBC driver not found: " + e.getMessage());
        }
    }
    
    public static void initialize() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            
            String createUsersTable = "CREATE TABLE IF NOT EXISTS " + TABLE_USERS + " (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "username TEXT NOT NULL UNIQUE," +
                    "password_hash TEXT NOT NULL," +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ")";
            stmt.execute(createUsersTable);
            
            String createPasswordsTable = "CREATE TABLE IF NOT EXISTS " + TABLE_PASSWORDS + " (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "user_id INTEGER NOT NULL," +
                    "service_name TEXT NOT NULL," +
                    "username TEXT NOT NULL," +
                    "password TEXT NOT NULL," +
                    "encryption_type TEXT NOT NULL," +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "FOREIGN KEY(user_id) REFERENCES " + TABLE_USERS + "(id) ON DELETE CASCADE" +
                    ")";
            stmt.execute(createPasswordsTable);
            
        } catch (SQLException e) {
            throw new RuntimeException("Database initialization error: " + e.getMessage());
        }
    }
    
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DATABASE_URL);
    }
    
    public static boolean registerUser(String username, String passwordHash) {
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                     "INSERT INTO " + TABLE_USERS + " (username, password_hash) VALUES (?, ?)")) {
            
            pstmt.setString(1, username);
            pstmt.setString(2, passwordHash);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            if (e.getMessage().contains("UNIQUE constraint failed")) {
                return false;
            }
            throw new RuntimeException("Registration error: " + e.getMessage());
        }
    }
    
    public static Integer authenticateUser(String username, String passwordHash) {
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                     "SELECT id FROM " + TABLE_USERS + " WHERE username = ? AND password_hash = ?")) {
            
            pstmt.setString(1, username);
            pstmt.setString(2, passwordHash);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Authentication error: " + e.getMessage());
        }
        return null;
    }
    
    public static boolean userExists(String username) {
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                     "SELECT 1 FROM " + TABLE_USERS + " WHERE username = ? LIMIT 1")) {
            
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("User check error: " + e.getMessage());
        }
    }
    
    public static void savePassword(int userId, String serviceName, String username, 
                                    String password, String encryptionType) {
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                     "INSERT INTO " + TABLE_PASSWORDS + 
                     " (user_id, service_name, username, password, encryption_type) " +
                     "VALUES (?, ?, ?, ?, ?)")) {
            
            pstmt.setInt(1, userId);
            pstmt.setString(2, serviceName);
            pstmt.setString(3, username);
            pstmt.setString(4, password);
            pstmt.setString(5, encryptionType);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Password save error: " + e.getMessage());
        }
    }
    
    public static List<PasswordEntry> getUserPasswords(int userId) {
        List<PasswordEntry> passwords = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                     "SELECT * FROM " + TABLE_PASSWORDS + " WHERE user_id = ? ORDER BY created_at DESC")) {
            
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    PasswordEntry entry = new PasswordEntry(
                            rs.getInt("id"),
                            rs.getString("service_name"),
                            rs.getString("username"),
                            rs.getString("password"),
                            rs.getString("encryption_type"),
                            rs.getTimestamp("created_at")
                    );
                    passwords.add(entry);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Password fetch error: " + e.getMessage());
        }
        return passwords;
    }
    
    public static void updatePassword(int passwordId, String password, String encryptionType) {
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                     "UPDATE " + TABLE_PASSWORDS + 
                     " SET password = ?, encryption_type = ?, updated_at = CURRENT_TIMESTAMP " +
                     "WHERE id = ?")) {
            
            pstmt.setString(1, password);
            pstmt.setString(2, encryptionType);
            pstmt.setInt(3, passwordId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Password update error: " + e.getMessage());
        }
    }
    
    public static void deletePassword(int passwordId) {
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                     "DELETE FROM " + TABLE_PASSWORDS + " WHERE id = ?")) {
            
            pstmt.setInt(1, passwordId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Password delete error: " + e.getMessage());
        }
    }
    
    public static class PasswordEntry {
        public final int id;
        public final String serviceName;
        public final String username;
        public final String password;
        public final String encryptionType;
        public final Timestamp createdAt;
        
        public PasswordEntry(int id, String serviceName, String username, 
                           String password, String encryptionType, Timestamp createdAt) {
            this.id = id;
            this.serviceName = serviceName;
            this.username = username;
            this.password = password;
            this.encryptionType = encryptionType;
            this.createdAt = createdAt;
        }
        
        @Override
        public String toString() {
            return serviceName + " (" + username + ")";
        }
    }
}
