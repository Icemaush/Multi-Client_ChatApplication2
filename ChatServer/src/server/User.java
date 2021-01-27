/*
Author: Reece Pieri
ID: M087496
Date: 25/09/2020
Assessment: Java III - Portfolio AT2 Q4
*/

package server;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public class User implements Serializable{
    private String username;
    private String passwordHash;
    private String salt;

    public User(String username, String password) {
        this.username = username;
        salt = generateSalt();
        passwordHash = hashPassword(password, salt);
    }
    
    // Generate password hash using SHA-512
    private String hashPassword(String password, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-512");
            md.update(Base64.getDecoder().decode(salt));
            byte[] hashedPassword = md.digest(password.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hashedPassword);
        } catch (NoSuchAlgorithmException ex) {
            System.out.println(ex.getMessage());
            return null;
        }
    }
    
    // Generate salt
    private String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] saltBytes = new byte[16];
        random.nextBytes(saltBytes);
        return Base64.getEncoder().encodeToString(saltBytes);
    }
    
    // Verify password
    public boolean verifyPassword(String password) {
        return passwordHash.equals(hashPassword(password, salt));
    }
    
    // Reset password
    public boolean resetPassword(String password) {
        setPasswordHash(hashPassword(password, salt));
        return true;
    }
    
// Getters and setters    
// <editor-fold>
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }
    
// </editor-fold>
}
