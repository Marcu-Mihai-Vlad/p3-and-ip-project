package org.example;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String Username;
    private String Password;
    private boolean isAdmin;

    public User() {}
    public User(String username, String password, boolean isAdmin) {
        this.Username = username;
        this.Password = password;
        this.isAdmin = isAdmin;
    }

    public Integer getId() {
        return this.id;
    }
    public void setId(Integer id) {
        this.id = id;
    }

    public String getUsername() {
        return this.Username;
    }
    public void setUsername(String username) {
        this.Username = username;
    }

    public boolean isAdmin() {
        return this.isAdmin;
    }
    public void setAdmin(boolean admin) {
        this.isAdmin = admin;
    }

    public void setPassword(String password) {
        this.Password = password;
    }
    public boolean checkPassword(String password) {
        return this.Password.equals(password);
    }

    @Override
    public String toString() {
        return "User{" + "Username=" + this.Username + ", Password=" + this.Password + ", isAdmin=" + this.isAdmin + '}';
    }
}
