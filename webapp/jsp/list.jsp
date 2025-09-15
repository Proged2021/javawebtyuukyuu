<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<!DOCTYPE html>
<html lang="ja">
<head>
<meta charset="UTF-8">
<title>予約一覧</title>
<link rel="stylesheet" href="<c:url value='/style.css'/>">
<style>
/* 既存 style.css を尊重しつつ、最小の上書きだけ */
.container { max-width: 1100px; margin: 0 auto; padding: 20px; }
.page-head { display:flex; align-items:center; gap:16px; margin-bottom:16px; }
.page-head h1 { margin:0; }
.toolbar { display:flex; gap:8px; flex-wrap:wrap; margin: 10px 0 18px; }
.search-sort-form { display:flex; gap:14px; flex-wrap:wrap; align-items:flex-end; margin: 10px 0 8px; }
.search-sort-form label { display:block; font-size: .9rem; color:#555; }
.search-sort-form input[type="text"], .search-sort-form select { padding:6px 8px; }
.table-actions form { display:inline; }
.badge { display:inline-block; font-size:.75rem; padding:2px 6px; border-radius:999px; background:#eef2ff; color:#3730a3; }
.subtle { color:#6b7280; font-size:.9rem; }
th.nowrap, td.nowrap { white-space:nowrap; }
th.small, td.small { width: 1%; }
th.price, td.price { text-align:right; white-space:nowrap; }
tfoot td { border-top: 1px solid #eee; padding-top:10px; }
.link-row { margin-left:auto; }
.notice { margin:10px 0; }
</style>
</head>
<body>
<div class="container">

  <div class="page-head">
    <h1>予約一覧</h1>
    <span class="badge">管理</span>
    <div class="link-row">
      <!-- クーポン管理（任意でリンク） -->
      <a href="<c:url value='/coupon?action=list'/>" class="button secondary">クーポン管理</a>
    </div>
  </div>

  <!-- メッセージ -->
  <c:if test="${not empty errorMessage}">
    <p class="error-message notice"><c:out value="${errorMessage}"/></p>
  </c:if>
  <c:if test="${not empty successMessage}">
    <p class="success-message notice"><c:out value="${successMessage}"/></p>
  </c:if>

  <!-- 検索／ソート -->
  <form action="<c:url value='/reservation'/>" method="get" class="search-sort-form">
    <input type="hidden" name="action" value="list">
    <div>
      <label for="search">検索</label>
      <input type="text" id="search" name="search" value="<c:out value='${searchTerm}'/>" placeholder="名前／日時／クーポン名／クーポンID">
    </div>
    <div>
      <label for="sortBy">ソート基準</label>
      <select id="sortBy" name="sortBy">
        <option value=""       <c:if test="${empty sortBy}">selected</c:if>>選択してください</option>
        <option value="name"   <c:if test="${sortBy == 'name'}">selected</c:if>>名前</option>
        <option value="time"   <c:if test="${sortBy == 'time'}">selected</c:if>>日時</option>
        <option value="coupon" <c:if test="${sortBy == 'coupon'}">selected</c:if>>クーポン</option>
      </select>
    </div>
    <div>
      <label for="sortOrder">ソート順</label>
      <select id="sortOrder" name="sortOrder">
        <option value="asc"  <c:if test="${sortOrder == 'asc'}">selected</c:if>>昇順</option>
        <option value="desc" <c:if test="${sortOrder == 'desc'}">selected</c:if>>降順</option>
      </select>
    </div>
    <div>
      <button type="submit" class="button">検索/ソート</button>
    </div>
  </form>

  <!-- アクション -->
  <div class="toolbar">
    <a href="<c:url value='/reservation?action=export_csv'/>" class="button">CSV エクスポート</a>
    <form action="<c:url value='/reservation'/>" method="get">
      <input type="hidden" name="action" value="clean_up">
      <input type="submit" value="過去の予約をクリーンアップ" class="button secondary"
             onclick="return confirm('本当に過去の予約を削除しますか？');">
    </form>
    <a href="<c:url value='/index.jsp'/>" class="button secondary">トップに戻る</a>
  </div>

  <!-- 一覧テーブル -->
  <table>
    <thead>
      <tr>
        <th class="small nowrap">ID</th>
        <th>名前</th>
        <th class="nowrap">予約日時</th>
        <th>クーポン</th>
        <th class="price small">金額</th>
        <th class="small nowrap">操作</th>
      </tr>
    </thead>
    <tbody>
      <c:forEach var="r" items="${reservations}">
        <tr>
          <td class="nowrap"><c:out value="${r.id}"/></td>
          <td>
            <div><c:out value="${r.name}"/></div>
            <div class="subtle">
              <!-- クーポンIDも補助表示（存在すれば） -->
              <c:if test="${not empty r.couponId}">
                ID: <c:out value="${r.couponId}"/>
              </c:if>
            </div>
          </td>
          <td class="nowrap"><c:out value="${r.reservationTime}"/></td>

          <!-- クーポン名（なければクーポンIDで代替／両方なければ「—」） -->
          <td>
            <c:choose>
              <c:when test="${not empty r.couponTitle}">
                <c:out value="${r.couponTitle}"/>
              </c:when>
              <c:when test="${not empty r.couponId}">
                クーポンID: <c:out value="${r.couponId}"/>
              </c:when>
              <c:otherwise>—</c:otherwise>
            </c:choose>
          </td>

          <!-- 金額（Integerで保存している想定。なければ「—」） -->
          <td class="price">
            <c:choose>
              <c:when test="${not empty r.couponPrice}">
                ￥<c:out value="${r.couponPrice}"/>
              </c:when>
              <c:otherwise>—</c:otherwise>
            </c:choose>
          </td>

          <td class="table-actions nowrap">
            <a href="<c:url value='/reservation?action=edit&id=${r.id}'/>" class="button">編集</a>
            <form action="<c:url value='/reservation'/>" method="post" style="display:inline;">
              <input type="hidden" name="action" value="delete">
              <input type="hidden" name="id" value="${r.id}">
              <input type="submit" value="キャンセル" class="button danger"
                     onclick="return confirm('本当にキャンセルしますか？');">
            </form>
          </td>
        </tr>
      </c:forEach>

      <c:if test="${empty reservations}">
        <tr><td colspan="6">予約がありません。</td></tr>
      </c:if>
    </tbody>
  </table>

  <!-- ページネーション -->
  <div class="pagination">
    <c:if test="${currentPage != 1}">
      <a href="<c:url value='/reservation?action=list&page=${currentPage - 1}&search=${fn:escapeXml(searchTerm)}&sortBy=${sortBy}&sortOrder=${sortOrder}'/>">前へ</a>
    </c:if>

    <c:forEach begin="1" end="${noOfPages}" var="i">
      <c:choose>
        <c:when test="${currentPage eq i}">
          <span class="current">${i}</span>
        </c:when>
        <c:otherwise>
          <a href="<c:url value='/reservation?action=list&page=${i}&search=${fn:escapeXml(searchTerm)}&sortBy=${sortBy}&sortOrder=${sortOrder}'/>">${i}</a>
        </c:otherwise>
      </c:choose>
    </c:forEach>

    <c:if test="${currentPage lt noOfPages}">
      <a href="<c:url value='/reservation?action=list&page=${currentPage + 1}&search=${fn:escapeXml(searchTerm)}&sortBy=${sortBy}&sortOrder=${sortOrder}'/>">次へ</a>
    </c:if>
  </div>

</div>
</body>
</html>
