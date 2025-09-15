<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="java.util.*, com.example.coupon.Coupon" %>
<%
    List<Coupon> coupons = (List<Coupon>) request.getAttribute("coupons");
    String ctx = request.getContextPath();
%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>クーポン一覧</title>
<style>
table { border-collapse: collapse; width: 100%; }
th, td { border: 1px solid #ccc; padding: 6px 8px; }
.actions a { margin-right: 6px; }
</style>
</head>
<body>
<h1>クーポン一覧</h1>

<form method="get" action="<%=ctx%>/coupon">
  <input type="hidden" name="action" value="list">
  <input type="text" name="search" placeholder="検索（タイトル/説明）">
  <select name="sortBy">
    <option value="id">ID</option>
    <option value="title">タイトル</option>
    <option value="price">価格</option>
    <option value="active">有効</option>
  </select>
  <select name="sortOrder">
    <option value="asc">昇順</option>
    <option value="desc">降順</option>
  </select>
  <button type="submit">検索/並べ替え</button>
</form>

<p>
  <a href="<%=ctx%>/coupon?action=edit">＋ 新規作成</a>
  |
  <a href="<%=ctx%>/coupon?action=export_csv">CSVエクスポート</a>
</p>

<form method="post" action="<%=ctx%>/coupon" enctype="multipart/form-data" style="margin:8px 0;">
  <input type="hidden" name="action" value="import_csv">
  <input type="file" name="csvFile" accept=".csv">
  <button type="submit">CSVインポート</button>
</form>

<table>
  <thead>
    <tr>
      <th>ID</th><th>タイトル</th><th>価格</th><th>有効</th><th>説明</th><th>操作</th>
    </tr>
  </thead>
  <tbody>
  <% if (coupons != null) for (Coupon c : coupons) { %>
    <tr>
      <td><%=c.getId()%></td>
      <td><%=c.getTitle()%></td>
      <td><%=c.getPrice()%></td>
      <td><%=c.isActive() ? "有効" : "無効"%></td>
      <td><%=c.getDescription()%></td>
      <td class="actions">
        <a href="<%=ctx%>/coupon?action=edit&id=<%=c.getId()%>">編集</a>
        <form method="post" action="<%=ctx%>/coupon" style="display:inline;">
          <input type="hidden" name="action" value="delete">
          <input type="hidden" name="id" value="<%=c.getId()%>">
          <button type="submit" onclick="return confirm('削除しますか？')">削除</button>
        </form>
      </td>
    </tr>
  <% } %>
  </tbody>
</table>

<p><a href="<%=ctx%>/reservation?action=list">← 予約一覧へ戻る</a></p>
</body>
</html>
