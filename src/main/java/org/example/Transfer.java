package org.example;

import jakarta.persistence.*;

@Entity
@DiscriminatorValue("transfer")
public class Transfer extends Transaction implements Applicable {

    @Column(name = "target_account_id")
    private Integer targetAccountId;

    public Transfer() {}

    public int getTargetAccountId() {
        return targetAccountId;
    }
    public void setTargetAccountId(int targetAccountId) {
        this.targetAccountId = targetAccountId;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + ", ID: " + this.accountID + " , Amount: " + this.balance +
                ", Target ID: " + this.targetAccountId + ", On " + this.date;

    }

    @Override
    public void applyToAccount(BankAccount bankAccount) {
        bankAccount.updateBalance(-balance);
    }
}
