package org.example;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@DiscriminatorValue("withdrawal")
public class Withdrawal extends Transaction implements Applicable {

    public Withdrawal() {}

    @Override
    public void applyToAccount(BankAccount bankAccount) {
        bankAccount.updateBalance(-balance);
    }
}
