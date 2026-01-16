package org.example;

import jakarta.persistence.*;


@Entity
@DiscriminatorValue("deposit")
public class Deposit extends Transaction implements Applicable {

    public Deposit() {}

    @Override
    public void applyToAccount(BankAccount bankAccount) {
        bankAccount.updateBalance(balance);
    }
}

