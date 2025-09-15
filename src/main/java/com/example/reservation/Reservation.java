package com.example.reservation;

import java.time.LocalDateTime;

/**
 * 予約エンティティ。
 * クーポン情報（ID/タイトル/価格）を保持できるように拡張しています。
 */
public class Reservation {
    private int id;
    private String name;
    private LocalDateTime reservationTime;

    // 追加: クーポン情報
    private Integer couponId;        // 例: 1,2,3 ... null 可
    private String  couponTitle;     // 例: "【新規】メンズカット＆パーマ"
    private Integer couponPrice;     // 例: 12320 / null 可

    public Reservation(int id, String name, LocalDateTime reservationTime) {
        this(id, name, reservationTime, null, null, null);
    }

    public Reservation(int id, String name, LocalDateTime reservationTime,
                       Integer couponId, String couponTitle, Integer couponPrice) {
        this.id = id;
        this.name = name;
        this.reservationTime = reservationTime;
        this.couponId = couponId;
        this.couponTitle = couponTitle;
        this.couponPrice = couponPrice;
    }

    // ---- getters ----
    public int getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public LocalDateTime getReservationTime() {
        return reservationTime;
    }
    public Integer getCouponId() {
        return couponId;
    }
    public String getCouponTitle() {
        return couponTitle;
    }
    public Integer getCouponPrice() {
        return couponPrice;
    }

    // ---- setters ----
    public void setId(int id) {
        this.id = id;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void setReservationTime(LocalDateTime reservationTime) {
        this.reservationTime = reservationTime;
    }
    public void setCouponId(Integer couponId) {
        this.couponId = couponId;
    }
    public void setCouponTitle(String couponTitle) {
        this.couponTitle = couponTitle;
    }
    public void setCouponPrice(Integer couponPrice) {
        this.couponPrice = couponPrice;
    }
}
