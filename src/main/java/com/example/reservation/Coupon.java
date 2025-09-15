package com.example.reservation;

import java.util.Objects;

/**
 * クーポンのシンプルなデータクラス（POJO）。
 * id: 一意なID
 * title: 表示名
 * time: 施術に要する時間（分）。0なら未設定扱い
 * price: 価格（円）。0なら未設定/割引クーポンなど
 */
public class Coupon {
    private final int id;
    private final String title;
    private final int time;   // minutes
    private final int price;  // JPY

    public Coupon(int id, String title, int time, int price) {
        this.id = id;
        this.title = title;
        this.time = time;
        this.price = price;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public int getTime() {
        return time;
    }

    public int getPrice() {
        return price;
    }

    // 便利メソッド（必須ではない）
    @Override
    public String toString() {
        return "Coupon{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", time=" + time +
                ", price=" + price +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Coupon)) return false;
        Coupon coupon = (Coupon) o;
        return id == coupon.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
