package com.android.montelongoworldwide.pages;

public class Transaction {
    public final int amount;
    public String id;
    public String purchaseAgreement;

    public Transaction(int amount, String id) {
        this.amount = amount;
        this.id = id;
    }
}
