package org.example;

import java.math.BigDecimal;
import java.math.RoundingMode;
import jakarta.persistence.*;

@Entity
@Table(name = "accounts")
public class BankAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer accountID;

    private double balance;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User User;

    public BankAccount() {}

    public double getBalance() {
        return this.balance;
    }
    public void updateBalance(double amount) {
        double sum = amount + this.balance;
        BigDecimal rounded = BigDecimal.valueOf(sum).setScale(2, RoundingMode.HALF_UP);
        this.balance = rounded.doubleValue();

    }
    public void setBalance(double balance) {
        this.balance = balance;
    }

    public int getAccountID() {
        return this.accountID;
    }
    public void setAccountID(int accountID) {
        this.accountID = accountID;
    }

    public User getUser() {
        return this.User;
    }
    public void setUser(User user) {
        this.User = user;
    }

    public void setPassword(String password) {
        this.getUser().setPassword(password);
    }

    public String getUsername() {
        return this.User.getUsername();
    }
    public void setUsername(String username) {
        this.User.setUsername(username);
    }

    @Override
    public String toString() {
        return "ID: " + this.accountID + ", Name: " + this.getUsername() + ", balance: " + this.balance;
    }
}
