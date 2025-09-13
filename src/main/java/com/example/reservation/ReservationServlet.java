package com.example.reservation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

@WebServlet("/reservation")
@MultipartConfig
public class ReservationServlet extends HttpServlet {
    private final ReservationDAO reservationDAO = new ReservationDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String action = req.getParameter("action");

        if ("list".equals(action) || action == null) {
            String searchTerm = req.getParameter("search");
            String sortBy = req.getParameter("sortBy");
            String sortOrder = req.getParameter("sortOrder");

            int page = 1;
            int recordsPerPage = 5;
            if (req.getParameter("page") != null) {
                try { page = Integer.parseInt(req.getParameter("page")); }
                catch (NumberFormatException e) { page = 1; }
            }

            List<Reservation> allReservations =
                    reservationDAO.searchAndSortReservations(searchTerm, sortBy, sortOrder);

            int start = (page - 1) * recordsPerPage;
            int end = Math.min(start + recordsPerPage, allReservations.size());
            List<Reservation> reservations = allReservations.subList(start, end);

            int noOfPages = (int) Math.ceil(allReservations.size() * 1.0 / recordsPerPage);

            req.setAttribute("reservations", reservations);
            req.setAttribute("noOfPages", noOfPages);
            req.setAttribute("currentPage", page);
            req.setAttribute("searchTerm", searchTerm);
            req.setAttribute("sortBy", sortBy);
            req.setAttribute("sortOrder", sortOrder);

            RequestDispatcher rd = req.getRequestDispatcher("/jsp/list.jsp");
            rd.forward(req, resp);
            return;
        }

        if ("edit".equals(action)) {
            try {
                int id = Integer.parseInt(req.getParameter("id"));
                Reservation reservation = reservationDAO.getReservationById(id);
                req.setAttribute("reservation", reservation);
                RequestDispatcher rd = req.getRequestDispatcher("/jsp/edit.jsp");
                rd.forward(req, resp);
            } catch (NumberFormatException e) {
                resp.sendRedirect("reservation?action=list");
            }
            return;
        }

        if ("export_csv".equals(action)) {
            exportCsv(req, resp);
            return;
        }

        if ("clean_up".equals(action)) {
            reservationDAO.cleanUpPastReservations();
            req.getSession().setAttribute("successMessage", "過去の予約をクリーンアップしました。");
            resp.sendRedirect("reservation?action=list");
            return;
        }

        // 2週間カレンダー
        if ("timeslots_week".equals(action)) {
            buildWeekCalendar(req, resp);
            return;
        }

        // デフォルト
        resp.sendRedirect("index.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        String action = req.getParameter("action");

        if ("add".equals(action)) {
            String name = req.getParameter("name");
            String reservationTimeString = req.getParameter("reservation_time");

            if (name == null || name.trim().isEmpty()) {
                req.setAttribute("errorMessage", "名前は必須です。");
                req.getRequestDispatcher("/index.jsp").forward(req, resp);
                return;
            }
            if (reservationTimeString == null || reservationTimeString.isEmpty()) {
                req.setAttribute("errorMessage", "希望日時は必須です。");
                req.getRequestDispatcher("/index.jsp").forward(req, resp);
                return;
            }
            try {
                LocalDateTime reservationTime = LocalDateTime.parse(reservationTimeString);
                if (reservationTime.isBefore(LocalDateTime.now())) {
                    req.setAttribute("errorMessage", "過去の日時は選択できません。");
                    req.getRequestDispatcher("/index.jsp").forward(req, resp);
                    return;
                }
                if (!reservationDAO.addReservation(name, reservationTime)) {
                    req.setAttribute("errorMessage", "同じ名前と日時での予約は既に存在します。");
                    req.getRequestDispatcher("/index.jsp").forward(req, resp);
                    return;
                }
                resp.sendRedirect("reservation?action=list");
            } catch (DateTimeParseException e) {
                req.setAttribute("errorMessage", "有効な日時を入力してください。");
                req.getRequestDispatcher("/index.jsp").forward(req, resp);
            }
            return;
        }

        if ("update".equals(action)) {
            try {
                int id = Integer.parseInt(req.getParameter("id"));
                String name = req.getParameter("name");
                String reservationTimeString = req.getParameter("reservation_time");

                if (name == null || name.trim().isEmpty()) {
                    req.setAttribute("errorMessage", "名前は必須です。");
                    req.getRequestDispatcher("/jsp/edit.jsp").forward(req, resp);
                    return;
                }
                if (reservationTimeString == null || reservationTimeString.trim().isEmpty()) {
                    req.setAttribute("errorMessage", "希望日時は必須です。");
                    req.getRequestDispatcher("/jsp/edit.jsp").forward(req, resp);
                    return;
                }

                LocalDateTime reservationTime = LocalDateTime.parse(reservationTimeString);
                if (reservationTime.isBefore(LocalDateTime.now())) {
                    req.setAttribute("errorMessage", "過去の日時は選択できません。");
                    req.getRequestDispatcher("/jsp/edit.jsp").forward(req, resp);
                    return;
                }
                if (!reservationDAO.updateReservation(id, name, reservationTime)) {
                    req.setAttribute("errorMessage", "同じ名前と日時での予約は既に存在します。");
                    req.getRequestDispatcher("/jsp/edit.jsp").forward(req, resp);
                    return;
                }
                resp.sendRedirect("reservation?action=list");
            } catch (NumberFormatException | DateTimeParseException e) {
                req.setAttribute("errorMessage", "有効な入力ではありません。");
                req.getRequestDispatcher("/jsp/edit.jsp").forward(req, resp);
            }
            return;
        }

        if ("delete".equals(action)) {
            try {
                int id = Integer.parseInt(req.getParameter("id"));
                reservationDAO.deleteReservation(id);
            } catch (NumberFormatException ignored) {}
            resp.sendRedirect("reservation?action=list");
            return;
        }

        if ("import_csv".equals(action)) {
            try {
                Part filePart = req.getPart("csvFile");
                if (filePart != null && filePart.getSize() > 0) {
                    try (BufferedReader reader = new BufferedReader(
                            new InputStreamReader(filePart.getInputStream(), "UTF-8"))) {
                        reservationDAO.importReservations(reader);
                        req.getSession().setAttribute("successMessage", "CSV ファイルのインポートが完了しました。");
                    }
                } else {
                    req.getSession().setAttribute("errorMessage", "インポートするファイルを選択してください。");
                }
            } catch (Exception e) {
                req.getSession().setAttribute("errorMessage", "CSV ファイルのインポート中にエラーが発生しました: " + e.getMessage());
                e.printStackTrace();
            }
            resp.sendRedirect("reservation?action=list");
            return;
        }

        // カレンダー「◎」→ 入力ページ
        if ("select_timeslot".equals(action)) {
            String date  = req.getParameter("date");
            String time  = req.getParameter("time");
            String title = req.getParameter("title");
            String price = req.getParameter("price");
            String ctime = req.getParameter("c_time");

            String ctx = req.getContextPath();
            String q = "?"
                + (date  != null ? "date="  + URLEncoder.encode(date,  StandardCharsets.UTF_8) + "&" : "")
                + (time  != null ? "start=" + URLEncoder.encode(time,  StandardCharsets.UTF_8) + "&" : "")
                + (title != null ? "title=" + URLEncoder.encode(title, StandardCharsets.UTF_8) + "&" : "")
                + (price != null ? "price=" + URLEncoder.encode(price, StandardCharsets.UTF_8) + "&" : "")
                + (ctime != null ? "time="  + URLEncoder.encode(ctime, StandardCharsets.UTF_8) : "");

            String target = ctx + "/jsp/input_member.jsp" + q;
            if (target.endsWith("&")) target = target.substring(0, target.length() - 1);

            resp.sendRedirect(resp.encodeRedirectURL(target));
            return;
        }

        // 入力 → 確認（POSTで必須データを渡す）
        if ("confirm_member".equals(action)) {
            RequestDispatcher rd = req.getRequestDispatcher("/jsp/confirm_member.jsp");
            rd.forward(req, resp);
            return;
        }

        // 確定
        if ("complete".equals(action)) {
            try {
                String name  = req.getParameter("name");
                String date  = req.getParameter("date");
                String start = req.getParameter("start");

                if (name == null || name.isBlank() || date == null || date.isBlank() || start == null || start.isBlank()) {
                    req.setAttribute("errorMessage", "入力が不足しています。もう一度やり直してください。");
                    req.getRequestDispatcher("/jsp/confirm_member.jsp").forward(req, resp);
                    return;
                }

                LocalDate d = LocalDate.parse(date);
                LocalTime t = LocalTime.parse(start.length()==5 ? start + ":00" : start);
                LocalDateTime when = LocalDateTime.of(d, t);

                if (!reservationDAO.isSlotAvailable(when)) {
                    req.setAttribute("errorMessage", "選択した日時は満席になりました。別の枠をお選びください。");
                    resp.sendRedirect(req.getContextPath()+"/reservation?action=timeslots_week&date="+date);
                    return;
                }

                boolean ok = reservationDAO.addReservation(name, when);
                if (!ok) {
                    req.setAttribute("errorMessage", "同じ名前と日時での予約は既に存在します。");
                    req.getRequestDispatcher("/jsp/confirm_member.jsp").forward(req, resp);
                    return;
                }

                req.setAttribute("name", name);
                req.setAttribute("when", when);
                req.getRequestDispatcher("/jsp/complete.jsp").forward(req, resp);
                return;

            } catch (Exception e) {
                e.printStackTrace();
                req.setAttribute("errorMessage", "予約確定でエラーが発生しました: " + e.getMessage());
                req.getRequestDispatcher("/jsp/confirm_member.jsp").forward(req, resp);
                return;
            }
        }

        // デフォルト
        resp.sendRedirect("index.jsp");
    }

    private void buildWeekCalendar(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        LocalDate startDate = LocalDate.now();
        LocalDate endDate   = startDate.plusDays(13);

        LocalTime startTime = LocalTime.of(9, 0);
        LocalTime endTime   = LocalTime.of(21, 0);
        LocalTime lateStart = LocalTime.of(20, 0);

        List<LocalTime> times = new ArrayList<>();
        for (LocalTime t = startTime; !t.isAfter(endTime); t = t.plusMinutes(30)) {
            times.add(t);
        }

        Map<LocalDate, Map<LocalTime, Boolean>> availability = new LinkedHashMap<>();
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            Map<LocalTime, Boolean> slots = new LinkedHashMap<>();
            boolean isMonday = (date.getDayOfWeek() == DayOfWeek.MONDAY);
            for (LocalTime time : times) {
                if (isMonday) {
                    slots.put(time, false);
                } else {
                    LocalDateTime slot = LocalDateTime.of(date, time);
                    boolean isFree = reservationDAO.isSlotAvailable(slot);
                    slots.put(time, isFree);
                }
            }
            availability.put(date, slots);
        }

        List<LocalDate> dates = new ArrayList<>(availability.keySet());

        Map<LocalDate, String> dowJa = new LinkedHashMap<>();
        for (LocalDate d : dates) {
            String w = switch (d.getDayOfWeek()) {
                case MONDAY    -> "(月)";
                case TUESDAY   -> "(火)";
                case WEDNESDAY -> "(水)";
                case THURSDAY  -> "(木)";
                case FRIDAY    -> "(金)";
                case SATURDAY  -> "(土)";
                case SUNDAY    -> "(日)";
            };
            dowJa.put(d, w);
        }

        DateTimeFormatter keyFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
        Map<String, Boolean> availKeyed = new LinkedHashMap<>();
        for (Map.Entry<LocalDate, Map<LocalTime, Boolean>> e : availability.entrySet()) {
            LocalDate d = e.getKey();
            for (Map.Entry<LocalTime, Boolean> s : e.getValue().entrySet()) {
                String key = d.atTime(s.getKey()).format(keyFmt);
                availKeyed.put(key, s.getValue());
            }
        }

        Map<LocalDate, Boolean> isSunday = new LinkedHashMap<>();
        Map<LocalDate, Boolean> isSaturday = new LinkedHashMap<>();
        for (LocalDate d : dates) {
            isSunday.put(d, d.getDayOfWeek() == DayOfWeek.SUNDAY);
            isSaturday.put(d, d.getDayOfWeek() == DayOfWeek.SATURDAY);
        }

        Map<LocalDate, Boolean> isToday = new LinkedHashMap<>();
        LocalDate today = LocalDate.now();
        for (LocalDate d : dates) {
            isToday.put(d, d.equals(today));
        }

        Map<String, Boolean> isPastKeyed = new LinkedHashMap<>();
        LocalDateTime now = LocalDateTime.now();
        for (LocalDate d : dates) {
            for (LocalTime t : times) {
                String key = d.atTime(t).format(keyFmt);
                isPastKeyed.put(key, d.atTime(t).isBefore(now));
            }
        }

        Map<String, Boolean> isLateKeyed = new LinkedHashMap<>();
        for (LocalDate d : dates) {
            for (LocalTime t : times) {
                String key = d.atTime(t).format(keyFmt);
                isLateKeyed.put(key, !t.isBefore(lateStart)); // 20:00以降
            }
        }

        String monthTitle;
        if (startDate.getYear() == endDate.getYear() && startDate.getMonth() == endDate.getMonth()) {
            monthTitle = String.format("%d年%d月", startDate.getYear(), startDate.getMonthValue());
        } else if (startDate.getYear() == endDate.getYear()) {
            monthTitle = String.format("%d年%d月〜%d月",
                    startDate.getYear(), startDate.getMonthValue(), endDate.getMonthValue());
        } else {
            monthTitle = String.format("%d年%d月〜%d年%d月",
                    startDate.getYear(), startDate.getMonthValue(),
                    endDate.getYear(), endDate.getMonthValue());
        }

        req.setAttribute("times", times);
        req.setAttribute("dates", dates);
        req.setAttribute("dowJa", dowJa);
        req.setAttribute("availKeyed", availKeyed);
        req.setAttribute("isSunday", isSunday);
        req.setAttribute("isSaturday", isSaturday);
        req.setAttribute("isToday", isToday);
        req.setAttribute("isPastKeyed", isPastKeyed);
        req.setAttribute("isLateKeyed", isLateKeyed);
        req.setAttribute("monthTitle", monthTitle);

        RequestDispatcher rd = req.getRequestDispatcher("/jsp/week_calendar.jsp");
        rd.forward(req, resp);
    }

    private void exportCsv(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("text/csv; charset=UTF-8");
        resp.setHeader("Content-Disposition", "attachment; filename=\"reservations.csv\"");

        try (PrintWriter writer = resp.getWriter()) {
            writer.println("ID,Name,ReservationTime");
            List<Reservation> reservations = reservationDAO.getAllReservations();
            for (Reservation res : reservations) {
                writer.printf("%d,%s,%s%n",
                        res.getId(),
                        res.getName(),
                        res.getReservationTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            }
        }
    }
}
