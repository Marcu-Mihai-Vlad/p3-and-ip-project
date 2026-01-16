package org.example;

import jakarta.persistence.*;

@Entity
@Table(name = "coupons")
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String store;
    private int percentage;

    public Coupon() {}
    public Coupon(String store, int percentage) {
        this.store = store;
        this.percentage = percentage;
    }

    public Integer getId() {
        return this.id;
    }
    public void setId(Integer id) {
        this.id = id;
    }
    public String getStore() {
        return this.store;
    }
    public void setStore(String store) {
        this.store = store;
    }
    public int getPercentage() {
        return this.percentage;
    }
    public void setPercentage(int percentage) {
        this.percentage = percentage;
    }

    @Override
    public String toString() {
        return "for " + this.store + " for " + this.percentage + "% off";
    }
}
