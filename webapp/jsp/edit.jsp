<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ page import="com.example.reservation.Reservation, java.util.*, com.example.coupon.Coupon" %>
<%
  String ctx = request.getContextPath();
  Reservation r = (Reservation) request.getAttribute("reservation");
  List<Coupon> coupons = (List<Coupon>) request.getAttribute("coupons");
%>
<!DOCTYPE html>
<html lang="ja">
<head>
<meta charset="UTF-8">
<title>予約編集</title>
<link rel="stylesheet" href="<%=ctx%>/style.css">
</head>
<body>
<div class="container">
  <h1>予約編集</h1>

  <p class="error-message"><c:out value="${errorMessage}" /></p>

  <form action="<%=ctx%>/reservation" method="post" class="form">
    <input type="hidden" name="action" value="update">
    <input type="hidden" name="id" value="<c:out value='${r.id}'/>">

    <div class="form-row">
      <label for="name">名前</label>
      <input type="text" id="name" name="name" value="<c:out value='${r.name}'/>" required>
    </div>

    <div class="form-row">
      <label for="reservation_time">予約日時</label>
      <!-- ブラウザのdatetime-localに合わせるなら必要に応じて整形。とりあえずISOのままでも動作します -->
      <input type="text" id="reservation_time" name="reservation_time"
             value="<c:out value='${r.reservationTime}'/>" required>
      <!-- 例: datetime-localを使いたい場合は type="datetime-local" に変更し、値を yyyy-MM-dd'T'HH:mm へ整形 -->
    </div>

    <div class="form-row">
      <label for="couponId">クーポン</label>
      <select id="couponId" name="couponId">
        <option value="">（クーポンなし）</option>
        <c:forEach var="c" items="${coupons}">
          <option value="${c.id}"
            <c:if test="${r.couponId != null && r.couponId == c.id}">selected</c:if>>
            <c:out value="${c.title}"/>（<c:out value="${c.price}"/>）
          </option>
        </c:forEach>
      </select>
    </div>

    <div class="button-group" style="margin-top:12px;">
      <button type="submit" class="button">更新</button>
      <a href="<%=ctx%>/reservation?action=list" class="button secondary">一覧へ戻る</a>
    </div>
  </form>
</div>
</body>
</html>
