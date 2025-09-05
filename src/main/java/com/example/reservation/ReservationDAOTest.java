package com.example.reservation;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class ReservationDAOTest {
    public static void main(String[] args) {
        ReservationDAO dao = new ReservationDAO();

        // ===== テスト1: 予約追加 =====
        System.out.println("=== テスト1: 予約追加 ===");
        LocalDateTime slot1 = LocalDateTime.of(2025, 9, 6, 10, 0);
        LocalDateTime slot2 = LocalDateTime.of(2025, 9, 7, 14, 30);

        dao.addReservation("テスト太郎", slot1);
        dao.addReservation("テスト花子", slot2);

        // 2025-09-06 の予約一覧を確認
        printReservationsByDate(dao, LocalDate.of(2025, 9, 6));
        // 2025-09-07 の予約一覧を確認
        printReservationsByDate(dao, LocalDate.of(2025, 9, 7));

        // ===== テスト2: 空き状況確認 =====
        System.out.println("\n=== テスト2: 空き状況確認 ===");
        System.out.println(slot1 + " の空き状況: " + (dao.isSlotAvailable(slot1) ? "◎ 空き" : "✗ 埋まり"));
        System.out.println(slot2 + " の空き状況: " + (dao.isSlotAvailable(slot2) ? "◎ 空き" : "✗ 埋まり"));
        LocalDateTime slot3 = LocalDateTime.of(2025, 9, 7, 15, 0);
        System.out.println(slot3 + " の空き状況: " + (dao.isSlotAvailable(slot3) ? "◎ 空き" : "✗ 埋まり"));

        // ===== テスト3: 予約削除 =====
        System.out.println("\n=== テスト3: 予約削除 ===");
        List<Reservation> day7Reservations = dao.getReservationsByDate(LocalDate.of(2025, 9, 7));
        if (!day7Reservations.isEmpty()) {
            int deleteId = day7Reservations.get(0).getId();
            System.out.println("ID " + deleteId + " の予約を削除します。");
            dao.deleteReservation(deleteId);
        }

        // 削除後の予約一覧を再確認
        printReservationsByDate(dao, LocalDate.of(2025, 9, 7));
    }

    // 1日の予約を一覧表示するヘルパーメソッド
    private static void printReservationsByDate(ReservationDAO dao, LocalDate date) {
        List<Reservation> reservations = dao.getReservationsByDate(date);
        System.out.println("【" + date + " の予約一覧】");
        if (reservations.isEmpty()) {
            System.out.println("  （予約なし）");
        } else {
            for (Reservation r : reservations) {
                System.out.println("  " + r.getId() + ": " + r.getName() + " / " + r.getReservationTime());
            }
        }
    }
}
