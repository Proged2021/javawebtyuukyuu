<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!doctype html>
<html lang="ja">
<head>
  <meta charset="utf-8"/>
  <meta name="viewport" content="width=device-width,initial-scale=1"/>
  <title>予約が完了しました</title>
  <script src="https://cdn.tailwindcss.com"></script>
</head>
<body class="bg-neutral-50">
  <div class="mx-auto max-w-xl mt-16 bg-white rounded-2xl shadow p-8 text-center">
    <h1 class="text-2xl font-bold text-pink-600">予約が完了しました</h1>
    <p class="mt-3 text-neutral-700">
      ご予約者様：<strong><c:out value="${name}"/></strong><br/>
      来店日時：<strong><c:out value="${when}"/></strong>
    </p>
    <div class="mt-6 flex justify-center gap-3">
      <a class="rounded-xl border px-4 py-2 hover:bg-neutral-100" href="${pageContext.request.contextPath}/shop-top/">お店ページへ</a>
      <a class="rounded-xl bg-pink-600 text-white px-4 py-2 hover:bg-pink-700"
         href="${pageContext.request.contextPath}/reservation?action=list">予約一覧へ</a>
    </div>
  </div>
</body>
</html>
