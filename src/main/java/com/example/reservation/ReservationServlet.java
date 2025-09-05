package com.example.reservation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
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

        //一覧
        if ("list".equals(action) || action == null) {
            String searchTerm = req.getParameter("search");
            String sortBy = req.getParameter("sortBy");
            String sortOrder = req.getParameter("sortOrder");

            int page = 1;
            int recordsPerPage = 5;
            if (req.getParameter("page") != null) {
                try {
                    page = Integer.parseInt(req.getParameter("page"));
                } catch (NumberFormatException e) {
                    page = 1;
                }
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

        //編集画面
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

        //CSVエクスポート
        if ("export_csv".equals(action)) {
            exportCsv(req, resp);
            return;
        }

        //過去データクリーンアップ
        if ("clean_up".equals(action)) {
            reservationDAO.cleanUpPastReservations();
            req.getSession().setAttribute("successMessage", "過去の予約をクリーンアップしました。");
            resp.sendRedirect("reservation?action=list");
            return;
        }

        //2週間カレンダー（今日〜13日後/計14日、30分刻み、月曜は全✗）
        if ("timeslots_week".equals(action)) {
            LocalDate startDate = LocalDate.now();
            LocalDate endDate   = startDate.plusDays(13);

            LocalTime startTime = LocalTime.of(9, 0);
            LocalTime endTime   = LocalTime.of(21, 0);

            // 縦軸の時間リスト（9:00〜21:00の30分刻み）
            List<LocalTime> times = new ArrayList<>();
            for (LocalTime t = startTime; !t.isAfter(endTime); t = t.plusMinutes(30)) {
                times.add(t);
            }

            // availability: 日付 → (時間 → 可否)
            Map<LocalDate, Map<LocalTime, Boolean>> availability = new LinkedHashMap<>();

            for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
                Map<LocalTime, Boolean> slots = new LinkedHashMap<>();
                boolean isMonday = (date.getDayOfWeek() == DayOfWeek.MONDAY);

                for (LocalTime time : times) {
                    if (isMonday) {
                        slots.put(time, false); // 定休日は全✗
                    } else {
                        LocalDateTime slot = LocalDateTime.of(date, time);
                        boolean isFree = reservationDAO.isSlotAvailable(slot);
                        slots.put(time, isFree);
                    }
                }
                availability.put(date, slots);
            }

            // JSPで扱いやすいように：日付の配列と、"yyyy-MM-dd'T'HH:mm" をキーにした可否表を用意
            List<LocalDate> dates = new ArrayList<>(availability.keySet());
            DateTimeFormatter keyFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
            Map<String, Boolean> availKeyed = new LinkedHashMap<>();
            for (Map.Entry<LocalDate, Map<LocalTime, Boolean>> e : availability.entrySet()) {
                LocalDate d = e.getKey();
                for (Map.Entry<LocalTime, Boolean> s : e.getValue().entrySet()) {
                    String key = d.atTime(s.getKey()).format(keyFmt); // 例: 2025-09-06T10:00
                    availKeyed.put(key, s.getValue());
                }
            }

            // JSPへ受け渡し
            req.setAttribute("startDate", startDate);
            req.setAttribute("endDate", endDate);
            req.setAttribute("times", times);
            req.setAttribute("dates", dates);
            req.setAttribute("availKeyed", availKeyed);

            RequestDispatcher rd = req.getRequestDispatcher("/jsp/week_calendar.jsp");
            rd.forward(req, resp);
            return;
        }

        // 6) デフォルト
        resp.sendRedirect("index.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        String action = req.getParameter("action");

        // 予約追加
        if ("add".equals(action)) {
            String name = req.getParameter("name");
            String reservationTimeString = req.getParameter("reservation_time");

            if (name == null || name.trim().isEmpty()) {
                req.setAttribute("errorMessage", "名前は必須です。");
                RequestDispatcher rd = req.getRequestDispatcher("/index.jsp");
                rd.forward(req, resp);
                return;
            }
            if (reservationTimeString == null || reservationTimeString.isEmpty()) {
                req.setAttribute("errorMessage", "希望日時は必須です。");
                RequestDispatcher rd = req.getRequestDispatcher("/index.jsp");
                rd.forward(req, resp);
                return;
            }
            try {
                LocalDateTime reservationTime = LocalDateTime.parse(reservationTimeString);
                if (reservationTime.isBefore(LocalDateTime.now())) {
                    req.setAttribute("errorMessage", "過去の日時は選択できません。");
                    RequestDispatcher rd = req.getRequestDispatcher("/index.jsp");
                    rd.forward(req, resp);
                    return;
                }
                if (!reservationDAO.addReservation(name, reservationTime)) {
                    req.setAttribute("errorMessage", "同じ名前と日時での予約は既に存在します。");
                    RequestDispatcher rd = req.getRequestDispatcher("/index.jsp");
                    rd.forward(req, resp);
                    return;
                }
                resp.sendRedirect("reservation?action=list");
            } catch (DateTimeParseException e) {
                req.setAttribute("errorMessage", "有効な日時を入力してください。");
                RequestDispatcher rd = req.getRequestDispatcher("/index.jsp");
                rd.forward(req, resp);
            }
            return;
        }

        // 予約更新
        if ("update".equals(action)) {
            try {
                int id = Integer.parseInt(req.getParameter("id"));
                String name = req.getParameter("name");
                String reservationTimeString = req.getParameter("reservation_time");

                if (name == null || name.trim().isEmpty()) {
                    req.setAttribute("errorMessage", "名前は必須です。");
                    RequestDispatcher rd = req.getRequestDispatcher("/jsp/edit.jsp");
                    rd.forward(req, resp);
                    return;
                }
                if (reservationTimeString == null || reservationTimeString.trim().isEmpty()) {
                    req.setAttribute("errorMessage", "希望日時は必須です。");
                    RequestDispatcher rd = req.getRequestDispatcher("/jsp/edit.jsp");
                    rd.forward(req, resp);
                    return;
                }

                LocalDateTime reservationTime = LocalDateTime.parse(reservationTimeString);
                if (reservationTime.isBefore(LocalDateTime.now())) {
                    req.setAttribute("errorMessage", "過去の日時は選択できません。");
                    RequestDispatcher rd = req.getRequestDispatcher("/jsp/edit.jsp");
                    rd.forward(req, resp);
                    return;
                }
                if (!reservationDAO.updateReservation(id, name, reservationTime)) {
                    req.setAttribute("errorMessage", "同じ名前と日時での予約は既に存在します。");
                    RequestDispatcher rd = req.getRequestDispatcher("/jsp/edit.jsp");
                    rd.forward(req, resp);
                    return;
                }
                resp.sendRedirect("reservation?action=list");
            } catch (NumberFormatException | DateTimeParseException e) {
                req.setAttribute("errorMessage", "有効な入力ではありません。");
                RequestDispatcher rd = req.getRequestDispatcher("/jsp/edit.jsp");
                rd.forward(req, resp);
            }
            return;
        }

        // 予約削除
        if ("delete".equals(action)) {
            try {
                int id = Integer.parseInt(req.getParameter("id"));
                reservationDAO.deleteReservation(id);
            } catch (NumberFormatException ignored) { }
            resp.sendRedirect("reservation?action=list");
            return;
        }

        // CSVインポート
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

        // デフォルト
        resp.sendRedirect("index.jsp");
    }

    // CSVエクスポート
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
