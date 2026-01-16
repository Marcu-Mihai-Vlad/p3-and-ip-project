package org.example;

import jakarta.persistence.*;
import java.time.LocalDateTime;


@Entity
@Table(name = "transactions")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "transaction_type")
public abstract class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_id", nullable = false)
    protected int accountID;

    @Column(name = "amount", nullable = false)
    protected double balance;

    @Column(name = "transaction_date", nullable = false)
    protected LocalDateTime date;

    @Override
    public String toString(){
        return this.getClass().getSimpleName() + ", " + "ID: " + this.accountID + ", Amount: " + this.balance + ", On " + this.date;
    }

    public int  getAccountID() {
        return accountID;
    }
    public void setAccountID(int accountID) {
        this.accountID = accountID;
    }

    public double getBalance() {
        return balance;
    }
    public void setBalance(double balance) {
        this.balance = balance;
    }

    public LocalDateTime getDate() {
        return date;
    }
    public void setDate(LocalDateTime date) {
        this.date = date;
    }
}
