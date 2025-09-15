package com.example.coupon;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

@WebServlet("/coupon")
@MultipartConfig
public class CouponServlet extends HttpServlet {
    private final CouponDAO dao = new CouponDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String a = req.getParameter("action");
        if (a == null || "list".equals(a)) {
            String search = req.getParameter("search");
            String sortBy = req.getParameter("sortBy");
            String sortOrder = req.getParameter("sortOrder");
            List<Coupon> items = dao.list(search, sortBy, sortOrder);
            req.setAttribute("coupons", items);
            RequestDispatcher rd = req.getRequestDispatcher("/jsp/coupon_list.jsp");
            rd.forward(req, resp);
            return;
        }
        if ("edit".equals(a)) {
            try {
                int id = Integer.parseInt(req.getParameter("id"));
                req.setAttribute("coupon", dao.get(id));
            } catch (Exception ignored) {}
            req.getRequestDispatcher("/jsp/coupon_edit.jsp").forward(req, resp);
            return;
        }
        if ("export_csv".equals(a)) {
            exportCsv(resp);
            return;
        }
        resp.sendRedirect(req.getContextPath() + "/coupon?action=list");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        String a = req.getParameter("action");

        if ("add".equals(a)) {
            String title = req.getParameter("title");
            String priceStr = req.getParameter("price");
            String description = req.getParameter("description");
            boolean active = "on".equals(req.getParameter("active"));

            if (title == null || title.isBlank() || priceStr == null || priceStr.isBlank()) {
                req.setAttribute("errorMessage", "タイトルと価格は必須です。");
                req.getRequestDispatcher("/jsp/coupon_edit.jsp").forward(req, resp);
                return;
            }
            try {
                int price = Integer.parseInt(priceStr);
                dao.add(title, price, description, active);
                resp.sendRedirect(req.getContextPath() + "/coupon?action=list");
                return;
            } catch (NumberFormatException e) {
                req.setAttribute("errorMessage", "価格は数値で入力してください。");
                req.getRequestDispatcher("/jsp/coupon_edit.jsp").forward(req, resp);
                return;
            }
        }

        if ("update".equals(a)) {
            try {
                int id = Integer.parseInt(req.getParameter("id"));
                String title = req.getParameter("title");
                int price = Integer.parseInt(req.getParameter("price"));
                String description = req.getParameter("description");
                boolean active = "on".equals(req.getParameter("active"));
                if (!dao.update(id, title, price, description, active)) {
                    req.setAttribute("errorMessage", "更新に失敗しました。");
                    req.getRequestDispatcher("/jsp/coupon_edit.jsp").forward(req, resp);
                    return;
                }
                resp.sendRedirect(req.getContextPath() + "/coupon?action=list");
                return;
            } catch (Exception e) {
                req.setAttribute("errorMessage", "入力が不正です。");
                req.getRequestDispatcher("/jsp/coupon_edit.jsp").forward(req, resp);
                return;
            }
        }

        if ("delete".equals(a)) {
            try {
                int id = Integer.parseInt(req.getParameter("id"));
                dao.delete(id);
            } catch (Exception ignored) {}
            resp.sendRedirect(req.getContextPath() + "/coupon?action=list");
            return;
        }

        if ("import_csv".equals(a)) {
            try {
                Part filePart = req.getPart("csvFile");
                if (filePart != null && filePart.getSize() > 0) {
                    try (BufferedReader reader = new BufferedReader(
                            new InputStreamReader(filePart.getInputStream(), StandardCharsets.UTF_8))) {
                        // フォーマット: id,title,price,active,description
                        String line;
                        while ((line = reader.readLine()) != null) {
                            String[] p = line.split(",", 5);
                            if (p.length < 5 || "id".equalsIgnoreCase(p[0])) continue; // ヘッダスキップ
                            try {
                                int id = Integer.parseInt(p[0]);
                                String title = p[1];
                                int price = Integer.parseInt(p[2]);
                                boolean active = Boolean.parseBoolean(p[3]);
                                String desc = p[4];
                                // 既存IDならupdate、無ければadd（idは採番し直し）
                                Coupon exist = dao.get(id);
                                if (exist == null) dao.add(title, price, desc, active);
                                else dao.update(id, title, price, desc, active);
                            } catch (NumberFormatException ignored) {}
                        }
                    }
                    req.getSession().setAttribute("successMessage", "CSVインポートが完了しました。");
                } else {
                    req.getSession().setAttribute("errorMessage", "CSVファイルを選択してください。");
                }
            } catch (Exception e) {
                req.getSession().setAttribute("errorMessage", "インポートでエラー: " + e.getMessage());
            }
            resp.sendRedirect(req.getContextPath() + "/coupon?action=list");
            return;
        }

        resp.sendRedirect(req.getContextPath() + "/coupon?action=list");
    }

    private void exportCsv(HttpServletResponse resp) throws IOException {
        resp.setContentType("text/csv; charset=UTF-8");
        resp.setHeader("Content-Disposition", "attachment; filename=\"coupons.csv\"");
        try (PrintWriter w = resp.getWriter()) {
            w.println("id,title,price,active,description");
            for (Coupon c : dao.list(null, null, null)) {
                w.printf("%d,%s,%d,%b,%s%n",
                        c.getId(), c.getTitle(), c.getPrice(), c.isActive(), c.getDescription());
            }
        }
    }
}
