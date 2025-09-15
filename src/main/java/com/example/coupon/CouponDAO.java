package com.example.coupon;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class CouponDAO {
    private static final List<Coupon> coupons = new CopyOnWriteArrayList<>();
    private static final AtomicInteger idCounter = new AtomicInteger(0);

    private static final Path DATA_DIR = Paths.get(System.getProperty("user.home"), ".javawebtyuukyuu");
    private static final Path DATA_FILE = DATA_DIR.resolve("coupons.csv");

    private static final ReentrantReadWriteLock RW = new ReentrantReadWriteLock();

    static { load(); }

    public List<Coupon> list(String search, String sortBy, String sortOrder) {
        RW.readLock().lock();
        try {
            List<Coupon> result = new ArrayList<>(coupons);
            // 検索（title/descriptionに含む）
            if (search != null && !search.isBlank()) {
                final String q = search.toLowerCase();
                result.removeIf(c ->
                    !(c.getTitle().toLowerCase().contains(q) ||
                      c.getDescription().toLowerCase().contains(q)));
            }
            // ソート
            Comparator<Coupon> cmp = Comparator.comparing(Coupon::getId);
            if ("title".equalsIgnoreCase(sortBy)) cmp = Comparator.comparing(Coupon::getTitle, String.CASE_INSENSITIVE_ORDER);
            if ("price".equalsIgnoreCase(sortBy)) cmp = Comparator.comparingInt(Coupon::getPrice);
            if ("active".equalsIgnoreCase(sortBy)) cmp = Comparator.comparing(Coupon::isActive);
            if ("desc".equalsIgnoreCase(sortOrder)) cmp = cmp.reversed();
            result.sort(cmp);
            return result;
        } finally { RW.readLock().unlock(); }
    }

    public Coupon get(int id) {
        RW.readLock().lock();
        try {
            return coupons.stream().filter(c -> c.getId() == id).findFirst().orElse(null);
        } finally { RW.readLock().unlock(); }
    }

    public boolean add(String title, int price, String description, boolean active) {
        RW.writeLock().lock();
        try {
            int id = idCounter.incrementAndGet();
            coupons.add(new Coupon(id, title, price, description, active));
            save();
            return true;
        } finally { RW.writeLock().unlock(); }
    }

    public boolean update(int id, String title, int price, String description, boolean active) {
        RW.writeLock().lock();
        try {
            for (int i = 0; i < coupons.size(); i++) {
                if (coupons.get(i).getId() == id) {
                    coupons.set(i, new Coupon(id, title, price, description, active));
                    save();
                    return true;
                }
            }
            return false;
        } finally { RW.writeLock().unlock(); }
    }

    public boolean delete(int id) {
        RW.writeLock().lock();
        try {
            boolean removed = coupons.removeIf(c -> c.getId() == id);
            if (removed) save();
            return removed;
        } finally { RW.writeLock().unlock(); }
    }

    private static void save() {
        RW.writeLock().lock();
        try {
            if (!Files.exists(DATA_DIR)) Files.createDirectories(DATA_DIR);
            Path tmp = DATA_DIR.resolve("coupons.tmp");
            try (BufferedWriter w = Files.newBufferedWriter(tmp, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
                for (Coupon c : coupons) {
                    // CSV: id,title,price,active,description（descriptionは末尾に置いてカンマを全角へ）
                    String safeTitle = sanitize(c.getTitle());
                    String safeDesc  = sanitize(c.getDescription());
                    w.write(String.format("%d,%s,%d,%b,%s%n",
                            c.getId(), safeTitle, c.getPrice(), c.isActive(), safeDesc));
                }
            }
            Files.move(tmp, DATA_FILE, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            System.err.println("Error saving coupons: " + e.getMessage());
        } finally { RW.writeLock().unlock(); }
    }

    private static void load() {
        RW.writeLock().lock();
        try {
            if (!Files.exists(DATA_FILE)) return;
            int maxId = 0;
            try (BufferedReader r = Files.newBufferedReader(DATA_FILE, StandardCharsets.UTF_8)) {
                String line;
                while ((line = r.readLine()) != null) {
                    String[] parts = line.split(",", 5); // descriptionにカンマがあってもOK
                    if (parts.length >= 5) {
                        try {
                            int id = Integer.parseInt(parts[0]);
                            String title = parts[1];
                            int price = Integer.parseInt(parts[2]);
                            boolean active = Boolean.parseBoolean(parts[3]);
                            String description = parts[4];
                            coupons.add(new Coupon(id, title, price, description, active));
                            if (id > maxId) maxId = id;
                        } catch (NumberFormatException e) {
                            System.err.println("Skip invalid line: " + line);
                        }
                    }
                }
            }
            idCounter.set(maxId);
        } catch (IOException e) {
            System.err.println("Error loading coupons: " + e.getMessage());
        } finally { RW.writeLock().unlock(); }
    }

    private static String sanitize(String s) {
        return s == null ? "" : s.replace(",", "，"); // カンマ→全角カンマ
    }
}
