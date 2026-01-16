package org.example;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@DiscriminatorValue("purchase")
public class Purchase extends Transaction implements Applicable {

    private String store;

    @Column(name = "coupon_id")
    private Integer couponId;


    public Purchase() {}

    public Integer getAppliedCoupon() {
        return couponId;
    }

    public void setAppliedCoupon(Integer appliedCoupon) {
        this.couponId = appliedCoupon;
    }
    public String getStore() {
        return store;
    }

    public void setStore(String store) {
        this.store = store;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + ", " + "ID: " + this.accountID + ", Amount: " + this.balance
                +", At: " + this.store + ", On " + this.date;

    }

    @Override
    public void applyToAccount(BankAccount bankAccount) {
        bankAccount.updateBalance(-balance);
    }
}
