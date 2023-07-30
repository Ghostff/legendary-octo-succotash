package com.android.montelongoworldwide.pages;

public class Transaction {
    public final int amount;
    public boolean isCompleted = false;
    public String id;
    public String purchaseAgreement;

    public Transaction(int amount) {
        this.amount = amount;
    }
}
