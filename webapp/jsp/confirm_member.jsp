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
        <input type="hidden" name="name"        id="hiddenName"/>
        <input type="hidden" name="date"        id="hiddenDate"/>
        <input type="hidden" name="start"       id="hiddenStart"/>
        <input type="hidden" name="coupon"      id="hiddenCoupon"/>
        <input type="hidden" name="member"      id="hiddenMember"/>
        <input type="hidden" name="timeslot"    id="hiddenTimeslot"/>
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

<!-- 受け取ったPOST値を安全に data-* に入れる -->
<div id="seed"
     data-member="${fn:escapeXml(param.member)}"
     data-coupon="${fn:escapeXml(param.coupon)}"
     data-timeslot="${fn:escapeXml(param.timeslot)}"
     data-date="${fn:escapeXml(param.date)}"
     data-start="${fn:escapeXml(param.start)}"
     style="display:none"></div>

<script>
  var yen = (n)=> new Intl.NumberFormat('ja-JP',{style:'currency',currency:'JPY'}).format(n);
  var parseMaybeJson=(s)=>{try{return s?JSON.parse(s):null;}catch(e){return null;}};
  var norm=(v)=> (v===false||v==='false'||v==null||v==='null'||v==='undefined')?'':String(v);

  var SEED=(document.getElementById('seed')||{}).dataset||{};
  var P_MEMBER=SEED.member||"",P_COUPON=SEED.coupon||"",P_TIMESLOT=SEED.timeslot||"",P_DATE=SEED.date||"",P_START=SEED.start||"";
  var qs=new URLSearchParams(location.search);

  // お客様情報
  (function(){
    var m = parseMaybeJson(P_MEMBER) || parseMaybeJson(qs.get('member')) || parseMaybeJson(sessionStorage.getItem('memberInfo')) || {};
    var set=(id,v)=>{var el=document.getElementById(id); if(el) el.textContent=v||'—';};
    var lastName=norm(m.lastName), firstName=norm(m.firstName), lastKana=norm(m.lastKana), firstKana=norm(m.firstKana);
    set('name',(lastName+' '+firstName).trim());
    set('kana',(lastKana+' '+firstKana).trim());
    set('tel',norm(m.tel)); set('email',norm(m.email));
    var gmap={female:'女性',male:'男性',other:'その他','':''}; set('gender', gmap[norm(m.gender)]||'—');
    set('purpose',norm(m.purpose)); set('stylist',norm(m.stylist)); set('notes',norm(m.notes));
    var hidden=document.getElementById('hiddenName'); if(hidden) hidden.value=((lastName+' '+firstName).trim())||norm(m.email);
    var hM=document.getElementById('hiddenMember'); if(hM) hM.value=JSON.stringify(m);
  })();

  // クーポン
  (function(){
    var c = parseMaybeJson(P_COUPON) || parseMaybeJson(qs.get('coupon')) || parseMaybeJson(sessionStorage.getItem('selectedCoupon')) || {};
    if(c && typeof c==='object'){
      if(c.title) document.getElementById('couponTitle').textContent=norm(c.title);
      if(c.time)  document.getElementById('couponTime').textContent='所要 '+norm(c.time)+'分';
      if(c.price) document.getElementById('couponPrice').textContent=yen(Number(c.price));
      var hC=document.getElementById('hiddenCoupon'); if(hC) hC.value=JSON.stringify(c);
    }
  })();

  // 来店日時
  (function(){
    var t = parseMaybeJson(P_TIMESLOT) || parseMaybeJson(qs.get('timeslot')) || parseMaybeJson(sessionStorage.getItem('selectedTimeslot'));
    var d='',s='';
    if(t && t.date && t.time){ d=norm(t.date); s=norm(t.time); }
    else{ d=norm(P_DATE)||norm(qs.get('date')); s=norm(P_START)||norm(qs.get('start')); }
    document.getElementById('visitDateTime').textContent=(d&&s)?(d+' '+s):'—';
    var hD=document.getElementById('hiddenDate');  if(hD) hD.value=d||'';
    var hS=document.getElementById('hiddenStart'); if(hS) hS.value=s||'';
    var hT=document.getElementById('hiddenTimeslot'); if(hT) hT.value=(t&&t.date&&t.time)?JSON.stringify(t):'';
  })();

  // 念のため action を絶対URL化
  document.getElementById('completeForm').action = location.origin + "<%= request.getContextPath() %>" + "/reservation";
</script>
</body>
</html>
