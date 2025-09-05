<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!doctype html>
<html lang="ja">
<head>
  <meta charset="UTF-8">
  <title>2週間 予約カレンダー</title>
  <style>
    body { font-family: system-ui, sans-serif; margin: 16px; }
    h1 { font-size: 18px; margin: 0 0 12px; }
    table { border-collapse: collapse; width: 100%; table-layout: fixed; }
    th, td { border: 1px solid #ccc; padding: 6px; text-align: center; }
    th { background: #f7f7f7; position: sticky; top: 0; }
    .time-col { width: 90px; background: #fafafa; position: sticky; left: 0; }
    .ok a { text-decoration: none; display: inline-block; width: 100%; }
    .x { color: #999; background: #f6f6f6; }
    .wrap { overflow: auto; max-height: calc(100vh - 160px); border: 1px solid #ddd; }
    .dow { font-size: 11px; color: #666; }
  </style>
</head>
<body>
  <h1>${startDate} 〜 ${endDate} の予約カレンダー（30分刻み）</h1>

  <div class="wrap">
    <table>
      <thead>
        <tr>
          <th class="time-col">時間</th>
          <c:forEach var="d" items="${dates}">
            <th>
              ${d}
              <div class="dow">${d.dayOfWeek}</div>
            </th>
          </c:forEach>
        </tr>
      </thead>
      <tbody>
        <c:forEach var="t" items="${times}">
          <tr>
            <td class="time-col">${t}</td>
            <c:forEach var="d" items="${dates}">
              <c:set var="key" value="${d}T${t}" />
              <c:set var="available" value="${availKeyed[key]}"/>
              <td class="${available ? 'ok' : 'x'}">
                <c:choose>
                  <c:when test="${available}">
                    <!-- いまは仮遷移。menu.jspが未作成なら404になるので、後で差し替えます -->
                    <a href="${pageContext.request.contextPath}/menu.jsp?dateTime=${key}">◎</a>
                  </c:when>
                  <c:otherwise>✗</c:otherwise>
                </c:choose>
              </td>
            </c:forEach>
          </tr>
        </c:forEach>
      </tbody>
    </table>
  </div>

  <p style="margin-top:8px; color:#666; font-size:12px;">
    ※ 月曜日は定休日（全✗）。「◎」クリックでメニュー選択へ（仮）。
  </p>
</body>
</html>
