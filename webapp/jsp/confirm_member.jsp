<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!doctype html>
<html lang="ja">
<head>
  <meta charset="utf-8"/>
  <meta name="viewport" content="width=device-width,initial-scale=1"/>
  <title>ご予約内容の確認</title>
  <script src="https://cdn.tailwindcss.com"></script>
  <style>
    .card{background:#fff;border:1px solid #f6dbe1;border-radius:16px;overflow:hidden}
    .head{background:#fdf2f8;color:#9f1239;font-weight:700;padding:.75rem 1rem;border-bottom:1px solid #fbe7ee}
    .pill{background:#fee2e2;color:#9f1239;font-weight:600;font-size:.75rem;padding:.125rem .5rem;border-radius:.5rem;}
    .line{display:flex;justify-content:space-between;gap:12px;padding:10px 0;border-bottom:1px solid #f3e2e7}
    .label{color:#6b7280;font-size:.875rem}
    .val{font-weight:600}
    .btn{border:none;border-radius:10px;padding:10px 14px;font-weight:700;cursor:pointer}
    .primary{background:#f472b6;color:#fff}
    .ghost{background:#fff;border:1px solid #e5e7eb}
  </style>
</head>
<body class="bg-neutral-50">
<header class="border-b bg-white">
  <div class="mx-auto flex max-w-6xl items-center gap-4 px-4 py-3">
    <div class="text-lg font-extrabold text-pink-600">B*beauty</div>
    <div class="ml-auto text-sm text-neutral-500">ご予約内容の確認</div>
  </div>
</header>

<main class="mx-auto max-w-6xl px-4 py-6 grid grid-cols-1 md:grid-cols-12 gap-6">
  <section class="md:col-span-8 space-y-6">
    <div class="card">
      <div class="head">お客様情報</div>
      <div class="p-5 space-y-2 text-sm">
        <div class="line"><div class="label">お名前</div><div class="val" id="name">—</div></div>
        <div class="line"><div class="label">フリガナ</div><div class="val" id="kana">—</div></div>
        <div class="line"><div class="label">携帯番号</div><div class="val" id="tel">—</div></div>
        <div class="line"><div class="label">メール</div><div class="val" id="email">—</div></div>
        <div class="line"><div class="label">性別</div><div class="val" id="gender">—</div></div>
        <div class="line"><div class="label">来店目的</div><div class="val" id="purpose">—</div></div>
        <div class="line"><div class="label">スタイリスト</div><div class="val" id="stylist">—</div></div>
        <div class="line" style="border-bottom:none"><div class="label">ご要望・連絡事項</div><div class="val text-right" id="notes" style="white-space:pre-wrap">—</div></div>
      </div>
    </div>

    <div class="flex items-center justify-end gap-3">
      <button type="button" class="btn ghost" onclick="history.back()">戻る</button>

      <form id="completeForm" method="post" action="${pageContext.request.contextPath}/reservation">
        <input type="hidden" name="action" value="complete"/>

        <!-- ここに hidden を追記 -->
        <input type="hidden" name="name"     id="hiddenName"/>
        <input type="hidden" name="member"   id="hiddenMember"/>
        <input type="hidden" name="couponId" id="hiddenCouponId"/>
        <input type="hidden" name="coupon"   id="hiddenCoupon"/>
        <input type="hidden" name="date"     id="hiddenDate"/>
        <input type="hidden" name="start"    id="hiddenStart"/>

        <button class="btn primary" type="submit">この内容で予約する</button>
      </form>
    </div>
  </section>

  <aside class="md:col-span-4">
    <div class="card sticky top-4">
      <div class="head">予約内容</div>
      <div class="p-5 space-y-3 text-sm">
        <div>
          <div class="label">クーポン</div>
          <div class="val" id="couponTitle">—</div>
          <div class="text-neutral-600"><span id="couponTime">—</span> / <span id="couponPrice">—</span></div>
        </div>
        <div class="h-px bg-neutral-200 my-2"></div>
        <div>
          <div class="label">来店日時</div>
          <div class="val" id="visitDateTime">—</div>
        </div>
        <div class="h-px bg-neutral-200 my-2"></div>
        <div class="flex flex-wrap gap-2">
          <span class="pill" id="pillMen">メンズ</span>
          <span class="pill" id="pillNew">新規</span>
        </div>
      </div>
    </div>
    <div class="text-xs text-neutral-500 mt-2">※ 「予約する」を押すと予約が確定します。</div>
  </aside>
</main>

<!-- seed: クーポン/日時など一時保存用 -->
<div id="seed"
     data-member="${fn:escapeXml(param.member)}"
     data-coupon="${fn:escapeXml(param.coupon)}"
     data-timeslot="${fn:escapeXml(param.timeslot)}"
     data-date="${fn:escapeXml(param.date)}"
     data-start="${fn:escapeXml(param.start)}"
     style="display:none"></div>

<!-- ===== ここから追加スクリプト ===== -->
<script>
(function(){
  const J = (s)=>{ try{return s?JSON.parse(s):null;}catch(e){return null;} };
  const N = (v)=> (v===false||v==='false'||v==null||v==='null'||v==='undefined') ? '' : String(v);
  const qs = new URLSearchParams(location.search);

  // --- お客様情報 ---
  const m = J(sessionStorage.getItem('memberInfo')) || {};
  const fullName = (N(m.lastName)+' '+N(m.firstName)).trim();
  const setText = (id, val) => { const el=document.getElementById(id); if(el) el.textContent=val||'—'; };
  setText('name', fullName || N(m.email));
  setText('kana', (N(m.lastKana)+' '+N(m.firstKana)).trim());
  setText('tel', N(m.tel));
  setText('email', N(m.email));
  setText('gender', ({male:'男性',female:'女性',other:'その他'})[N(m.gender)]||'');
  setText('purpose', N(m.purpose));
  setText('stylist', N(m.stylist));
  setText('notes', N(m.notes));

  const hName=document.getElementById('hiddenName'); if(hName) hName.value=fullName||N(m.email)||'来店者';
  const hMember=document.getElementById('hiddenMember'); if(hMember) hMember.value=JSON.stringify(m);

  // --- クーポン ---
  const seed=document.getElementById('seed');
  const rawCoupon=(seed&&seed.dataset?seed.dataset.coupon:null)||qs.get('coupon')||sessionStorage.getItem('selectedCoupon');
  const c=J(rawCoupon)||{};
  const idRaw=(c.id!=null)?String(c.id):'';
  const idNum=idRaw.replace(/\D+/g,''); // "c2" → "2"
  if(c.title) document.getElementById('couponTitle').textContent=N(c.title);
  if(c.time)  document.getElementById('couponTime').textContent='所要 '+N(c.time)+'分';
  if(c.price) document.getElementById('couponPrice').textContent=new Intl.NumberFormat('ja-JP',{style:'currency',currency:'JPY'}).format(c.price);

  const hCid=document.getElementById('hiddenCouponId'); if(hCid) hCid.value=idNum;
  const hC=document.getElementById('hiddenCoupon'); if(hC) hC.value=JSON.stringify(c);

  // --- 日時 ---
  const rawTimeslot=(seed&&seed.dataset?seed.dataset.timeslot:null)||qs.get('timeslot')||sessionStorage.getItem('selectedTimeslot');
  const t=J(rawTimeslot)||{};
  const d=N(t.date)||N(seed.dataset.date)||qs.get('date');
  const s=N(t.time)||N(seed.dataset.start)||qs.get('start');
  const visit=document.getElementById('visitDateTime'); if(visit) visit.textContent=(d&&s)?(d+' '+s):'—';

  const hD=document.getElementById('hiddenDate'); if(hD) hD.value=d||'';
  const hS=document.getElementById('hiddenStart'); if(hS) hS.value=s||'';
})();
</script>
<!-- ===== 追加スクリプトここまで ===== -->

</body>
</html>
