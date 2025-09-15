<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="com.example.coupon.Coupon" %>
<%
  String ctx = request.getContextPath();
  Coupon c = (Coupon) request.getAttribute("coupon");
  boolean editing = (c != null);
  String error = (String) request.getAttribute("errorMessage");
%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title><%= editing ? "クーポン編集" : "クーポン新規" %></title>
</head>
<body>
<h1><%= editing ? "クーポン編集" : "クーポン新規" %></h1>

<% if (error != null) { %>
  <p style="color:red;"><%=error%></p>
<% } %>

<form method="post" action="<%=ctx%>/coupon">
  <input type="hidden" name="action" value="<%= editing ? "update" : "add" %>">
  <% if (editing) { %>
    <input type="hidden" name="id" value="<%=c.getId()%>">
  <% } %>

  <div>
    <label>タイトル</label><br>
    <input type="text" name="title" value="<%= editing ? c.getTitle() : "" %>" required>
  </div>
  <div>
    <label>価格</label><br>
    <input type="number" name="price" value="<%= editing ? c.getPrice() : 0 %>" required>
  </div>
  <div>
    <label>説明</label><br>
    <textarea name="description" rows="4" cols="60"><%= editing ? c.getDescription() : "" %></textarea>
  </div>
  <div>
    <label><input type="checkbox" name="active" <%= editing && c.isActive() ? "checked" : "" %> > 有効</label>
  </div>

  <div style="margin-top:8px;">
    <button type="submit"><%= editing ? "更新" : "作成" %></button>
    <a href="<%=ctx%>/coupon?action=list">一覧へ戻る</a>
  </div>
</form>
</body>
</html>
