package com.example.coupon;

public class Coupon {
    private final int id;
    private final String title;
    private final int price;          // 割引額 or メニュー価格、運用に合わせて名称変更可
    private final String description; // 説明
    private final boolean active;     // 有効/無効

    public Coupon(int id, String title, int price, String description, boolean active) {
        this.id = id;
        this.title = title;
        this.price = price;
        this.description = description;
        this.active = active;
    }
    public int getId() { return id; }
    public String getTitle() { return title; }
    public int getPrice() { return price; }
    public String getDescription() { return description; }
    public boolean isActive() { return active; }
}
