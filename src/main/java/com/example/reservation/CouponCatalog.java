package com.example.reservation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * アプリ内で使う固定クーポンのカタログ。
 * ここに追加すれば、全画面で同じ定義を共有できます。
 */
public class CouponCatalog {

    private static final List<Coupon> coupons = new ArrayList<>();

    static {
        // --- ここにクーポンを追加（例） ---

        coupons.add(new Coupon(
                1,
                "【新規】メンズカット＆パーマ",
                120,   // 分
                12320  // 円
        ));

        coupons.add(new Coupon(
                2,
                "【学生限定】全メニュー10％オフ",
                0,
                0
        ));

        coupons.add(new Coupon(
                3,
                "【新規】メンズカット＋カラー",
                120,
                13960
        ));

        coupons.add(new Coupon(
                4,
                "【新規】縮毛矯正＋カット",
                180,
                19800
        ));

        coupons.add(new Coupon(
                5,
                "【再来】ビジネスマン向け20％オフ",
                0,
                0
        ));

        coupons.add(new Coupon(
                6,
                "【期間限定】トリートメント無料",
                30,
                0
        ));

        // 追加したいときは以下に増やしてください
        // coupons.add(new Coupon(7, "〇〇", 90, 11000));
    }

    /** クーポン一覧（読み取り専用） */
    public static List<Coupon> getCoupons() {
        return Collections.unmodifiableList(coupons);
    }

    /** ID からクーポンを取得（見つからなければ null） */
    public static Coupon getCouponById(int id) {
        return coupons.stream()
                .filter(c -> c.getId() == id)
                .findFirst()
                .orElse(null);
    }
}
