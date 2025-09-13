<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c"  uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!doctype html>
<html lang="ja">
<head>
  <meta charset="utf-8"/>
  <meta name="viewport" content="width=device-width, initial-scale=1"/>
  <title>空席カレンダー</title>
  <style>
    :root{
      --rose-50:#fdf2f8; --rose-100:#fde2e7;
      --neutral-100:#f5f5f5; --neutral-200:#e5e7eb;
    }
    html,body{margin:0;background:#fafafa;color:#111;font-family:system-ui,-apple-system,"Segoe UI",Roboto,"Hiragino Kaku Gothic ProN","Yu Gothic",Meiryo,sans-serif;}
    .wrap{max-width:1200px;margin:0 auto;padding:16px;}
    .head{display:flex;align-items:center;gap:12px;margin-bottom:12px}
    .brand{color:#e11d48;font-weight:800}
    .title{font-weight:700;font-size:20px}
    .calendar{background:#fff;border:1px solid var(--rose-100);border-radius:14px;overflow:hidden;box-shadow:0 1px 8px #0000000d}
    .grid{border-collapse:separate;border-spacing:0;width:100%;table-layout:fixed}
    .grid th,.grid td{border-right:1px solid var(--neutral-200);border-bottom:1px solid var(--neutral-200)}
    .grid th:last-child,.grid td:last-child{border-right:none}
    .grid thead th{position:sticky;top:0;background:#fff;z-index:1}
    .dow{font-size:12px;color:#64748b}
    .date{font-weight:700}
    .today{background:var(--neutral-100)}
    .sat .date{color:#2563eb}
    .sun .date{color:#ef4444}
    .time-col{width:86px;background:#fff;position:sticky;left:0;z-index:2}
    .time-col .t{font-size:12px;color:#475569}
    .time-row{background:#fff}
    .late{background:linear-gradient(90deg, #ffffff 0%, #f7f7f7 100%)}
    .cell{height:44px;text-align:center;padding:4px}
    .btn{display:inline-flex;align-items:center;justify-content:center;border:none;border-radius:10px;padding:8px 10px;cursor:pointer;font-size:13px;font-weight:700;min-width:48px}
    .btn-ok{background:#10b981;color:#fff}
    .btn-ng{background:#e5e7eb;color:#94a3b8;cursor:not-allowed}
    .badge{display:inline-block;border-radius:8px;padding:2px 8px;background:var(--rose-50);color:#9f1239;font-weight:700;font-size:12px;border:1px solid var(--rose-100)}
    .bar{display:flex;justify-content:space-between;align-items:center;margin:10px 0 14px}
    .note{font-size:12px;color:#64748b}
  </style>
</head>
<body>
<div class="wrap">
  <div class="head">
    <div class="brand">B*beauty</div>
    <div class="title">空席カレンダー</div>
  </div>

  <div class="bar">
    <div>
      <span class="badge">${monthTitle}</span>
      <c:if test="${not empty param.title}">
        <span class="badge">クーポン: ${param.title}</span>
      </c:if>
    </div>
    <div class="note">※ 月曜は終日× / 30分刻み</div>
  </div>

  <div class="calendar">
    <table class="grid">
      <thead>
      <tr>
        <th class="time-col today"><div style="padding:8px 6px">時間</div></th>
        <c:forEach var="d" items="${dates}">
          <c:set var="dateStr" value="${d}"/>
          <c:set var="isSun" value="${isSunday[d]}"/>
          <c:set var="isSat" value="${isSaturday[d]}"/>
          <c:set var="isTod" value="${isToday[d]}"/>
          <th class="${isSun?'sun':''} ${isSat?'sat':''} ${isTod?'today':''}">
            <div style="padding:8px 6px;text-align:center">
              <div class="date">${fn:substring(dateStr,5,10)}</div>
              <div class="dow">${dowJa[d]}</div>
            </div>
          </th>
        </c:forEach>
      </tr>
      </thead>

      <tbody>
      <c:forEach var="t" items="${times}">
        <c:set var="timeStr" value="${fn:substring(t,0,5)}"/>
        <tr class="time-row">
          <td class="time-col"><div class="t" style="padding:8px 6px;">${timeStr}</div></td>

          <c:forEach var="d" items="${dates}">
            <c:set var="dateStr" value="${d}"/>
            <c:set var="key" value="${dateStr}T${timeStr}"/>
            <c:set var="free" value="${availKeyed[key]}"/>
            <c:set var="past" value="${isPastKeyed[key]}"/>
            <c:set var="late" value="${isLateKeyed[key]}"/>

            <td class="cell ${late ? 'late' : ''}">
              <c:choose>
                <c:when test="${free and not past}">
                  <form method="post" action="${pageContext.request.contextPath}/reservation" style="display:inline-block">
                    <input type="hidden" name="action" value="select_timeslot"/>
                    <input type="hidden" name="date"   value="${dateStr}"/>
                    <input type="hidden" name="time"   value="${timeStr}"/>
                    <input type="hidden" name="title"  value="${param.title}"/>
                    <input type="hidden" name="price"  value="${param.price}"/>
                    <input type="hidden" name="c_time" value="${param.time}"/>
                    <button type="submit" class="btn btn-ok" aria-label="この枠を予約する">◎</button>
                  </form>
                </c:when>
                <c:otherwise>
                  <button type="button" class="btn btn-ng" disabled>×</button>
                </c:otherwise>
              </c:choose>
            </td>
          </c:forEach>

        </tr>
      </c:forEach>
      </tbody>
    </table>
  </div>

  <div style="margin-top:14px">
    <a href="${pageContext.request.contextPath}/shop-top/" style="font-size:12px;color:#2563eb;text-decoration:none;">← お店ページに戻る</a>
  </div>
</div>
</body>
</html>
