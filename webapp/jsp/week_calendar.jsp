<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!doctype html>
<html lang="ja">
<head>
  <meta charset="UTF-8">
  <title>日時を選択</title>
  <style>
    :root{
      --accent:#ff6f61; --accent-weak:#ffe4e0; --grid:#e6e6e6;
      --text:#333; --muted:#888; --xbg:#f4f5f7; --sun:#d9534f; --sat:#1e88e5;
      --today-bg:#fff7f5; /* 当日列の淡いハイライト */
    }
    *{box-sizing:border-box}
    body{
      font-family:system-ui,"Hiragino Kaku Gothic ProN","Yu Gothic",Meiryo,sans-serif;
      color:var(--text); margin:16px;
    }
    .heading{display:flex;align-items:baseline;gap:10px;margin:0 auto 8px;max-width:980px}
    .title{font-size:17px;font-weight:700}
    .month{font-size:13px;color:var(--muted)}

    .legend{
      display:flex;gap:10px;align-items:center;font-size:12px;color:var(--muted);
      margin:6px auto 10px; max-width:980px;
    }
    .legend .pill{border:1px solid var(--grid);padding:1px 8px;border-radius:999px;min-width:24px;text-align:center}
    .legend .ok{border-color:var(--accent);color:var(--accent);background:var(--accent-weak)}

    .wrap{
      overflow:auto; border:1px solid var(--grid); border-radius:10px;
      box-shadow:0 2px 6px rgba(0,0,0,.04);
      max-width:980px; margin:0 auto; background:#fff;
    }

    table{border-collapse:separate;border-spacing:0;width:100%;table-layout:fixed}
    th,td{border-bottom:1px solid var(--grid);border-right:1px solid var(--grid);text-align:center}
    th:first-child,td:first-child{border-left:1px solid var(--grid)}
    thead th{position:sticky;top:0;background:#fff;z-index:2}
    .time-col{
      width:60px; position:sticky; left:0; background:#fff; z-index:3;
      font-weight:600; font-size:12px; padding:4px 2px;
    }

    /* 日付ヘッダ：上=日、下=曜日。todayで淡く背景 */
    .date-head{padding:4px 2px; min-width:58px}
    .date-head .day{font-size:15px;font-weight:700;line-height:1}
    .date-head .dow{font-size:10px;color:var(--muted);margin-top:2px}
    .date-head.sunday .day,.date-head.sunday .dow{color:var(--sun)}
    .date-head.saturday .day,.date-head.saturday .dow{color:var(--sat)}
    .date-head.today{background:var(--today-bg)}

    tbody td{height:28px;padding:0}
    .ok a{
      display:flex; align-items:center; justify-content:center;
      width:100%; height:100%; text-decoration:none;
      color:var(--accent); font-weight:700; font-size:12px;
      transition:background .12s, transform .04s;
      border-radius:999px; margin:2px;
    }
    .ok a:hover{background:var(--accent-weak)}
    .ok a:active{transform:scale(0.98)}
    .x{background:var(--xbg); color:var(--muted); font-size:12px}
    .past{opacity:.45; pointer-events:none}
    .late{opacity:.7} /* 閉店前の遅い時間帯をややトーンダウン */

    /* 角丸 */
    table thead tr th:first-child{border-top-left-radius:10px}
    table thead tr th:last-child{border-top-right-radius:10px}
    table tbody tr:last-child td:first-child{border-bottom-left-radius:10px}
    table tbody tr:last-child td:last-child{border-bottom-right-radius:10px}

    /* さらに小さくしたい時は body に dense クラス */
    body.dense .wrap{max-width:860px}
    body.dense .date-head{min-width:50px}
    body.dense .time-col{width:54px}
    body.dense tbody td{height:26px}
    body.dense .ok a{margin:1px; font-size:11px}
  </style>
</head>
<body>
  <!-- 上段：タイトルと年月 -->
  <div class="heading">
    <div class="title">日時を選択</div>
    <div class="month">${monthTitle}</div>
  </div>

  <div class="legend">
    <span class="pill ok">◎ 予約可</span>
    <span class="pill">✗ 予約不可</span>
    <span>※ 月曜日は定休日（全て ✗）</span>
  </div>

  <div class="wrap">
    <table>
      <thead>
        <tr>
          <th class="time-col">時間</th>
          <c:forEach var="d" items="${dates}">
            <th class="date-head
                       ${isSunday[d] ? 'sunday' : ''} 
                       ${isSaturday[d] ? 'saturday' : ''} 
                       ${isToday[d] ? 'today' : ''}">
              <!-- 上：日（2桁）、下：曜日 -->
              <div class="day">
                <c:choose>
                  <c:when test="${d.dayOfMonth lt 10}">0${d.dayOfMonth}</c:when>
                  <c:otherwise>${d.dayOfMonth}</c:otherwise>
                </c:choose>
              </div>
              <div class="dow">${dowJa[d]}</div>
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
              <c:set var="isPast" value="${isPastKeyed[key]}"/>
              <c:set var="isLate" value="${isLateKeyed[key]}"/>

              <td class="${available ? 'ok' : 'x'} ${isPast ? 'past' : ''} ${isLate ? 'late' : ''}">
                <c:choose>
                  <c:when test="${available && !isPast}">
                    <!-- menu.jsp は仮遷移（未作成なら404） -->
                    <a href="${pageContext.request.contextPath}/menu.jsp?dateTime=${key}"
                       aria-label="${d} ${t} を予約">◎</a>
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
</body>
</html>
