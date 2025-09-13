<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!doctype html>
<html lang="ja">
<head>
  <meta charset="utf-8"/>
  <meta name="viewport" content="width=device-width,initial-scale=1"/>
  <title>管理ダッシュボード</title>
  <style>
    .wrap{max-width:960px;margin:0 auto;padding:16px;background:#fafafa}
    .grid{display:grid;grid-template-columns:repeat(3,minmax(0,1fr));gap:12px}
    .card{background:#fff;border:1px solid #e5e7eb;border-radius:14px;padding:14px}
    .btn{display:inline-block;margin-top:8px;border-radius:10px;background:#f472b6;color:#fff;padding:8px 12px;text-decoration:none}
    .ghost{border:1px solid #e5e7eb;background:#fff;color:#111}
  </style>
</head>
<body class="wrap">
  <h1 style="margin:8px 0 14px;color:#e11d48">管理ダッシュボード</h1>
  <div class="grid">
    <div class="card">
      <h3>予約一覧・検索</h3>
      <p>ページング／検索／並び替え</p>
      <a class="btn" href="${pageContext.request.contextPath}/reservation?action=list">開く</a>
    </div>
    <div class="card">
      <h3>CSV 書き出し</h3>
      <p>全予約を CSV でダウンロード</p>
      <a class="btn" href="${pageContext.request.contextPath}/reservation?action=export_csv">エクスポート</a>
    </div>
    <div class="card">
      <h3>CSV 取り込み</h3>
      <form method="post" action="${pageContext.request.contextPath}/reservation?action=import_csv" enctype="multipart/form-data">
        <input type="file" name="csvFile" accept=".csv" />
        <button class="btn" type="submit">インポート</button>
      </form>
    </div>
    <div class="card">
      <h3>過去予約のクリーンアップ</h3>
      <p>現在日時より前の予約を削除</p>
      <a class="btn ghost" href="${pageContext.request.contextPath}/reservation?action=clean_up">実行</a>
    </div>
  </div>

  <div style="margin-top:16px">
    <a href="${pageContext.request.contextPath}/shop-top/" style="color:#2563eb">← お店ページへ</a>
  </div>
</body>
</html>
