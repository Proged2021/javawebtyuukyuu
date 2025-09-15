package com.example.reservation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * インメモリ + ファイル保存型のDAO。
 * 予約CSV/データファイルにクーポン列（couponId, couponTitle, couponPrice）を追加。
 * さらに、couponTitle が空だった場合は CouponCatalog から補完します（方法1）。
 */
public class ReservationDAO {
    private static final List<Reservation> reservations = new CopyOnWriteArrayList<>();
    private static final AtomicInteger idCounter = new AtomicInteger(0);

    // 保存先（CSV）: id,name,time,couponId,couponTitle,couponPrice
    private static final String DATA_FILE = "reservations.dat";

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    static {
        loadReservations();
    }

    /* ========================= 基本CRUD ========================= */

    public List<Reservation> getAllReservations() {
        // 取得時にも念のため補完
        List<Reservation> copy = new ArrayList<>(reservations);
        enrichWithCouponInfo(copy);
        return copy;
    }

    public Reservation getReservationById(int id) {
        return reservations.stream()
                .filter(r -> r.getId() == id)
                .findFirst()
                .orElse(null);
    }

    /** 旧: 2引数版（互換性用） */
    public boolean addReservation(String name, LocalDateTime reservationTime) {
        return addReservation(name, reservationTime, null, null, null);
    }

    /** 新: クーポン情報付き */
    public boolean addReservation(String name,
                                  LocalDateTime reservationTime,
                                  Integer couponId,
                                  String couponTitle,
                                  Integer couponPrice) {
        // 重複チェック（名前 + 時刻）
        if (isDuplicate(name, reservationTime)) {
            return false;
        }
        int id = idCounter.incrementAndGet();

        // タイトルが空の場合は補完
        if (couponTitle == null || couponTitle.isBlank()) {
            if (couponId != null) {
                Coupon c = CouponCatalog.getCouponById(couponId);
                if (c != null) {
                    couponTitle = c.getTitle();
                    if (couponPrice == null) {
                        couponPrice = c.getPrice();
                    }
                }
            }
        }

        reservations.add(new Reservation(id, name, reservationTime, couponId, couponTitle, couponPrice));
        saveReservations();
        return true;
    }

    /** 旧: 3引数版（互換性用） */
    public boolean updateReservation(int id, String name, LocalDateTime reservationTime) {
        return updateReservation(id, name, reservationTime, null, null, null);
    }

    /** 新: クーポン情報付き */
    public boolean updateReservation(int id,
                                     String name,
                                     LocalDateTime reservationTime,
                                     Integer couponId,
                                     String couponTitle,
                                     Integer couponPrice) {
        if (isDuplicate(name, reservationTime, id)) {
            return false;
        }
        for (int i = 0; i < reservations.size(); i++) {
            if (reservations.get(i).getId() == id) {
                // タイトルが空の場合は補完
                if (couponTitle == null || couponTitle.isBlank()) {
                    if (couponId != null) {
                        Coupon c = CouponCatalog.getCouponById(couponId);
                        if (c != null) {
                            couponTitle = c.getTitle();
                            if (couponPrice == null) {
                                couponPrice = c.getPrice();
                            }
                        }
                    }
                }
                reservations.set(i, new Reservation(id, name, reservationTime, couponId, couponTitle, couponPrice));
                saveReservations();
                return true;
            }
        }
        return false;
    }

    public boolean deleteReservation(int id) {
        boolean removed = reservations.removeIf(r -> r.getId() == id);
        if (removed) {
            saveReservations();
        }
        return removed;
    }

    public void cleanUpPastReservations() {
        int initialSize = reservations.size();
        reservations.removeIf(r -> r.getReservationTime().isBefore(LocalDateTime.now()));
        if (reservations.size() < initialSize) {
            saveReservations();
        }
    }

    // ユーザーがクリックした枠が予約可能かどうか判定
    public boolean isSlotAvailable(LocalDateTime slot) {
        return reservations.stream()
                .noneMatch(r -> r.getReservationTime().equals(slot));
    }

    // 指定日の予約リストを取得
    public List<Reservation> getReservationsByDate(LocalDate date) {
        return reservations.stream()
                .filter(r -> r.getReservationTime().toLocalDate().equals(date))
                .collect(Collectors.toList());
    }

    /**
     * 検索 + ソート。検索対象に couponTitle/couponId も含める。
     * sortBy: "name" | "time" | "coupon"
     */
    public List<Reservation> searchAndSortReservations(String searchTerm, String sortBy, String sortOrder) {
        String term = searchTerm == null ? "" : searchTerm.trim().toLowerCase();

        List<Reservation> filteredList = reservations.stream()
                .filter(r -> {
                    if (term.isEmpty()) return true;
                    // 名前 / 日時 / タイトル / ID で検索
                    boolean hit = false;
                    if (r.getName() != null && r.getName().toLowerCase().contains(term)) hit = true;
                    if (!hit && r.getReservationTime() != null &&
                            r.getReservationTime().format(FORMATTER).toLowerCase().contains(term)) hit = true;
                    if (!hit && r.getCouponTitle() != null &&
                            r.getCouponTitle().toLowerCase().contains(term)) hit = true;
                    if (!hit && r.getCouponId() != null &&
                            String.valueOf(r.getCouponId()).contains(term)) hit = true;
                    return hit;
                })
                .collect(Collectors.toList());

        Comparator<Reservation> comparator = null;
        if ("name".equalsIgnoreCase(sortBy)) {
            comparator = Comparator.comparing(Reservation::getName, Comparator.nullsLast(String::compareToIgnoreCase));
        } else if ("time".equalsIgnoreCase(sortBy)) {
            comparator = Comparator.comparing(Reservation::getReservationTime, Comparator.nullsLast(LocalDateTime::compareTo));
        } else if ("coupon".equalsIgnoreCase(sortBy)) {
            comparator = Comparator.comparing(
                    (Reservation r) -> Objects.toString(r.getCouponTitle(), ""),
                    String.CASE_INSENSITIVE_ORDER
            ).thenComparing(r -> Objects.toString(r.getCouponId(), ""));
        }

        if (comparator != null) {
            if ("desc".equalsIgnoreCase(sortOrder)) {
                filteredList.sort(comparator.reversed());
            } else {
                filteredList.sort(comparator);
            }
        }

        // 返却前に補完
        enrichWithCouponInfo(filteredList);
        return filteredList;
    }

    /* ========================= CSV Import ========================= */

    /**
     * CSV を取り込み。
     * ヘッダー想定:
     * - 旧形式: id,name,time
     * - 新形式: id,name,time,couponId,couponTitle,couponPrice
     * 取り込んだ後、couponTitle が空で couponId がある場合は CouponCatalog で補完。
     */
    public void importReservations(BufferedReader reader) throws IOException {
        String line;
        boolean first = true;
        while ((line = reader.readLine()) != null) {
            if (first) { // 先頭行がヘッダならスキップ
                first = false;
                if (line.toLowerCase().contains("reservationtime")) {
                    continue;
                }
            }
            if (line.trim().isEmpty()) continue;

            String[] parts = line.split(",", -1); // 空欄も保持
            try {
                if (parts.length < 3) continue;

                int id = Integer.parseInt(parts[0].trim());
                String name = parts[1].trim();
                LocalDateTime time = LocalDateTime.parse(parts[2].trim(), FORMATTER);

                Integer couponId = null;
                String couponTitle = null;
                Integer couponPrice = null;

                if (parts.length >= 4 && !parts[3].trim().isEmpty()) {
                    String onlyNum = parts[3].trim().replaceAll("\\D+", "");
                    if (!onlyNum.isEmpty()) couponId = Integer.valueOf(onlyNum);
                }
                if (parts.length >= 5 && !parts[4].trim().isEmpty()) {
                    couponTitle = parts[4].trim();
                }
                if (parts.length >= 6 && !parts[5].trim().isEmpty()) {
                    String digits = parts[5].trim().replaceAll("[^0-9-]", "");
                    if (!digits.isEmpty()) couponPrice = Integer.valueOf(digits);
                }

                // タイトルが空で ID がある場合は補完
                if ((couponTitle == null || couponTitle.isBlank()) && couponId != null) {
                    Coupon c = CouponCatalog.getCouponById(couponId);
                    if (c != null) {
                        couponTitle = c.getTitle();
                        if (couponPrice == null) couponPrice = c.getPrice();
                    }
                }

                // 既存と重複しない場合のみ追加
                if (getReservationById(id) == null) {
                    reservations.add(new Reservation(id, name, time, couponId, couponTitle, couponPrice));
                    if (id > idCounter.get()) idCounter.set(id);
                }
            } catch (NumberFormatException | DateTimeParseException e) {
                System.err.println("Skipping invalid CSV line: " + line + " - " + e.getMessage());
            }
        }
        saveReservations();
    }

    /* ========================= 内部ユーティリティ ========================= */

    private boolean isDuplicate(String name, LocalDateTime time) {
        return reservations.stream()
                .anyMatch(r -> r.getName().equalsIgnoreCase(name) &&
                        r.getReservationTime().equals(time));
    }

    private boolean isDuplicate(String name, LocalDateTime time, int excludeId) {
        return reservations.stream()
                .anyMatch(r -> r.getId() != excludeId &&
                        r.getName().equalsIgnoreCase(name) &&
                        r.getReservationTime().equals(time));
    }

    /** 予約リスト全体に対してクーポン情報を補完（方法1の要） */
    private void enrichWithCouponInfo(List<Reservation> list) {
        for (Reservation r : list) {
            enrichOne(r);
        }
    }

    /** 1件分を補完 */
    private void enrichOne(Reservation r) {
        if (r == null) return;
        if (r.getCouponId() == null) return;
        if (r.getCouponTitle() != null && !r.getCouponTitle().isBlank()) return;

        Coupon c = CouponCatalog.getCouponById(r.getCouponId());
        if (c != null) {
            r.setCouponTitle(c.getTitle());
            if (r.getCouponPrice() == null) {
                r.setCouponPrice(c.getPrice());
            }
        }
    }

    /* ========================= ファイル保存/読み込み ========================= */

    private static void saveReservations() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(DATA_FILE))) {
            for (Reservation res : reservations) {
                String cid = res.getCouponId() == null ? "" : String.valueOf(res.getCouponId());
                String cti = res.getCouponTitle() == null ? "" : res.getCouponTitle();
                String cpr = res.getCouponPrice() == null ? "" : String.valueOf(res.getCouponPrice());
                writer.write(String.format("%d,%s,%s,%s,%s,%s%n",
                        res.getId(),
                        res.getName(),
                        res.getReservationTime().format(FORMATTER),
                        cid, cti.replace(",", "、"), cpr));
            }
        } catch (IOException e) {
            System.err.println("Error saving reservations: " + e.getMessage());
        }
    }

    private static void loadReservations() {
        File file = new File(DATA_FILE);
        if (!file.exists()) {
            return;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(DATA_FILE))) {
            String line;
            int maxId = 0;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",", -1); // 空欄も保持
                if (parts.length >= 3) {
                    try {
                        int id = Integer.parseInt(parts[0].trim());
                        String name = parts[1].trim();
                        LocalDateTime time = LocalDateTime.parse(parts[2].trim(), FORMATTER);

                        Integer couponId = null;
                        String couponTitle = null;
                        Integer couponPrice = null;

                        if (parts.length >= 4 && !parts[3].trim().isEmpty()) {
                            String onlyNum = parts[3].trim().replaceAll("\\D+", "");
                            if (!onlyNum.isEmpty()) couponId = Integer.valueOf(onlyNum);
                        }
                        if (parts.length >= 5 && !parts[4].trim().isEmpty()) {
                            couponTitle = parts[4].trim();
                        }
                        if (parts.length >= 6 && !parts[5].trim().isEmpty()) {
                            String digits = parts[5].trim().replaceAll("[^0-9-]", "");
                            if (!digits.isEmpty()) couponPrice = Integer.valueOf(digits);
                        }

                        Reservation r = new Reservation(id, name, time, couponId, couponTitle, couponPrice);

                        // タイトル補完（方法1）
                        if ((r.getCouponTitle() == null || r.getCouponTitle().isBlank()) && r.getCouponId() != null) {
                            Coupon c = CouponCatalog.getCouponById(r.getCouponId());
                            if (c != null) {
                                r.setCouponTitle(c.getTitle());
                                if (r.getCouponPrice() == null) r.setCouponPrice(c.getPrice());
                            }
                        }

                        reservations.add(r);
                        if (id > maxId) {
                            maxId = id;
                        }
                    } catch (NumberFormatException e) {
                        System.err.println("Skipping invalid data file line(NumberFormatException): " + line + " - " + e.getMessage());
                    } catch (DateTimeParseException e) {
                        System.err.println("Skipping invalid data file line(DateTimeParseException): " + line + " - " + e.getMessage());
                    }
                }
            }
            idCounter.set(maxId);
        } catch (IOException e) {
            System.err.println("Error loading reservations (IOException): " + e.getMessage());
        } catch (Exception e) {
            System.err.println("An unexpected error occurred while loading reservations: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
