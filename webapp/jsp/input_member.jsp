<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<!doctype html>
<html lang="ja">
<head>
  <meta charset="utf-8" />
  <meta name="viewport" content="width=device-width,initial-scale=1" />
  <title>お客様情報入力 - 予約</title>
  <script src="https://cdn.tailwindcss.com"></script>
  <style>
    .section-card { border: 1px solid #f6dbe1; border-radius: 16px; background: #fff }
    .section-head { background:#fdf2f8; color:#9f1239; font-weight:600; padding:.75rem 1rem; border-bottom:1px solid #fbe7ee; border-top-left-radius:16px; border-top-right-radius:16px;}
    .btn-primary { background:#f472b6; color:#fff; }
    .btn-primary:hover { background:#ec5aa9; }
    .pill { background:#fee2e2; color:#9f1239; font-weight:600; font-size:.75rem; padding:.125rem .5rem; border-radius:.5rem;}
    .req::after{content:" *"; color:#e11d48; font-weight:700;}
    .hint{font-size:.75rem; color:#6b7280;}
    .error{font-size:.8rem; color:#b91c1c; display:none;}
    .field{display:block;}
  </style>
</head>
<body class="bg-neutral-50">
  <header class="border-b bg-white">
    <div class="mx-auto flex max-w-6xl items-center gap-4 px-4 py-3">
      <div class="text-lg font-extrabold text-pink-600">B*beauty</div>
      <div class="ml-auto text-sm text-neutral-500">お客様情報入力</div>
    </div>
  </header>

  <main class="mx-auto max-w-6xl px-4 py-6 grid grid-cols-1 md:grid-cols-12 gap-6">
    <section class="md:col-span-8 space-y-6">
      <div class="section-card">
        <div class="section-head">ご来店者さま情報</div>
        <div class="p-5 space-y-4">
          <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <label class="field">
              <span class="text-sm req">お名前（姓）</span>
              <input id="lastName" name="lastName" type="text" class="mt-1 w-full rounded border px-3 py-2" autocomplete="family-name" required />
              <div class="hint">例：山田</div>
              <div class="error" id="err_lastName">姓を入力してください</div>
            </label>
            <label class="field">
              <span class="text-sm req">お名前（名）</span>
              <input id="firstName" name="firstName" type="text" class="mt-1 w-full rounded border px-3 py-2" autocomplete="given-name" required />
              <div class="hint">例：太郎</div>
              <div class="error" id="err_firstName">名を入力してください</div>
            </label>
          </div>

          <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <label class="field">
              <span class="text-sm">フリガナ（セイ）</span>
              <input id="lastKana" name="lastKana" type="text" class="mt-1 w-full rounded border px-3 py-2" />
            </label>
            <label class="field">
              <span class="text-sm">フリガナ（メイ）</span>
              <input id="firstKana" name="firstKana" type="text" class="mt-1 w-full rounded border px-3 py-2" />
            </label>
          </div>

          <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <label class="field">
              <span class="text-sm req">携帯番号</span>
              <input id="tel" name="tel" type="tel" inputmode="tel" class="mt-1 w-full rounded border px-3 py-2" placeholder="09012345678" required />
              <div class="hint">ハイフンなし</div>
              <div class="error" id="err_tel">携帯番号（10〜11桁）を入力してください</div>
            </label>
            <label class="field">
              <span class="text-sm req">メールアドレス</span>
              <input id="email" name="email" type="email" class="mt-1 w-full rounded border px-3 py-2" placeholder="you@example.com" required />
              <div class="error" id="err_email">メールアドレスの形式で入力してください</div>
            </label>
          </div>

          <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <label class="field">
              <span class="text-sm">性別</span>
              <div class="mt-1 flex gap-4 text-sm">
                <label class="flex items-center gap-2"><input type="radio" name="gender" value="female" />女性</label>
                <label class="flex items-center gap-2"><input type="radio" name="gender" value="male" />男性</label>
                <label class="flex items-center gap-2"><input type="radio" name="gender" value="other" />その他</label>
                <label class="flex items-center gap-2"><input type="radio" name="gender" value="" />回答しない</label>
              </div>
            </label>

            <label class="field">
              <span class="text-sm">来店目的</span>
              <select id="purpose" class="mt-1 w-full rounded border px-3 py-2">
                <option value="">選択してください</option>
                <option>イメチェンしたい</option>
                <option>メンテナンス（伸びた分）</option>
                <option>イベント前に整えたい</option>
                <option>担当者の提案に任せたい</option>
              </select>
            </label>
          </div>

          <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <label class="field">
              <span class="text-sm">希望スタイリスト</span>
              <select id="stylist" class="mt-1 w-full rounded border px-3 py-2">
                <option value="">指定なし</option>
                <option>指名あり（+¥550）</option>
                <option>男性スタイリスト希望</option>
                <option>女性スタイリスト希望</option>
              </select>
            </label>
            <label class="field">
              <span class="text-sm">ご来店人数</span>
              <select id="party" class="mt-1 w-full rounded border px-3 py-2">
                <option>1名</option><option>2名</option><option>3名</option>
              </select>
            </label>
          </div>

          <label class="field">
            <span class="text-sm">ご要望・連絡事項</span>
            <textarea id="notes" rows="4" class="mt-1 w-full rounded border px-3 py-2" placeholder="髪の悩み、NG事項、到着が遅れそうな場合の連絡 など"></textarea>
          </label>
        </div>
      </div>

      <div class="section-card">
        <div class="section-head">利用規約・個人情報の取り扱い</div>
        <div class="p-5 space-y-3">
          <label class="flex items-start gap-3 text-sm">
            <input id="agree" type="checkbox" class="mt-1" />
            <span>
              <span class="req">規約に同意する</span><br />
              <span class="hint">予約に関する連絡のため、入力情報はサロンへ提供されます。</span>
            </span>
          </label>
          <div class="error" id="err_agree">規約への同意が必要です</div>
        </div>
      </div>

      <div class="flex items-center justify-end gap-3">
        <button type="button" class="rounded px-4 py-2 border hover:bg-neutral-50" onclick="history.back()">戻る</button>
        <button id="submitBtn" class="btn-primary rounded px-5 py-2.5">確認画面へ</button>
      </div>
    </section>

    <aside class="md:col-span-4">
      <div class="section-card sticky top-4">
        <div class="section-head">予約内容の確認</div>
        <div class="p-5 space-y-3 text-sm">
          <div>
            <div class="text-neutral-500">クーポン</div>
            <div id="couponTitle" class="font-semibold">—</div>
            <div class="text-neutral-600"><span id="couponTime">—</span> / <span id="couponPrice">—</span></div>
          </div>
          <div class="h-px bg-neutral-200 my-2"></div>
          <div>
            <div class="text-neutral-500">来店日時</div>
            <div id="visitDateTime" class="font-medium">—</div>
            <div class="hint">※ カレンダーで選択した日時（未選択の場合は空欄）</div>
          </div>
          <div class="h-px bg-neutral-200 my-2"></div>
          <div class="flex flex-wrap gap-2">
            <span class="pill" id="pillMen">メンズ</span>
            <span class="pill" id="pillNew">新規</span>
          </div>
        </div>
      </div>
    </aside>
  </main>

  <script>
    const jpYen = (n) => new Intl.NumberFormat('ja-JP', { style:'currency', currency:'JPY' }).format(n);
    const parseMaybeJson = (s) => { try { return s ? JSON.parse(s) : null; } catch(e) { return null; } };

    // 右側の表示をURL/SSから再現
    (function hydrate(){
      try {
        const params = new URLSearchParams(location.search);
        const s = sessionStorage.getItem('selectedCoupon');
        const coupon = s ? JSON.parse(s) : null;
        const title = params.get('title') || (coupon && coupon.title) || '';
        const price = Number(params.get('price') || (coupon && coupon.price) || 0);
        const time  = Number(params.get('time')  || (coupon && coupon.time)  || 0);
        document.getElementById('couponTitle').textContent = title || '—';
        document.getElementById('couponPrice').textContent = price ? jpYen(price) : '—';
        document.getElementById('couponTime').textContent  = time ? `所要 ${time}分` : '—';

        const date = params.get('date') || '';
        const start= params.get('start') || '';
        if (date && start) {
          document.getElementById('visitDateTime').textContent = `${date} ${start}`;
        } else {
          const ts = sessionStorage.getItem('selectedTimeslot');
          if (ts) {
            const { date: d, time: t } = JSON.parse(ts);
            document.getElementById('visitDateTime').textContent = d && t ? `${d} ${t}` : '—';
          }
        }
      } catch (e) { console.warn(e); }
    })();

    // 確認画面へ（POSTで送る）
    document.getElementById('submitBtn').addEventListener('click', function(){
      ['err_lastName','err_firstName','err_tel','err_email','err_agree'].forEach(id => {
        const el = document.getElementById(id); if (el) el.style.display = 'none';
      });

      const lastName  = document.getElementById('lastName').value.trim();
      const firstName = document.getElementById('firstName').value.trim();
      const tel       = document.getElementById('tel').value.trim();
      const email     = document.getElementById('email').value.trim();
      const agree     = document.getElementById('agree').checked;

      let ok = true;
      if (!lastName){ document.getElementById('err_lastName').style.display='block'; ok=false; }
      if (!firstName){ document.getElementById('err_firstName').style.display='block'; ok=false; }
      if (!/^\d{10,11}$/.test(tel)){ document.getElementById('err_tel').style.display='block'; ok=false; }
      if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)){ document.getElementById('err_email').style.display='block'; ok=false; }
      if (!agree){ document.getElementById('err_agree').style.display='block'; ok=false; }
      if (!ok) return;

      const payload = {
        lastName, firstName,
        lastKana:  document.getElementById('lastKana').value.trim(),
        firstKana: document.getElementById('firstKana').value.trim(),
        tel, email,
        gender:    (document.querySelector('input[name="gender"]:checked')||{}).value || '',
        purpose:   document.getElementById('purpose').value,
        stylist:   document.getElementById('stylist').value,
        party:     document.getElementById('party').value,
        notes:     document.getElementById('notes').value,
      };
      sessionStorage.setItem('memberInfo', JSON.stringify(payload));

      const selectedCoupon   = sessionStorage.getItem('selectedCoupon');
      const selectedTimeslot = sessionStorage.getItem('selectedTimeslot');
      const urlq = new URLSearchParams(location.search);
      const date  = urlq.get('date')  || '';
      const start = urlq.get('start') || '';

      const ctx = "/" + location.pathname.split("/")[1];
      const form = document.createElement('form');
      form.method = 'post';
      form.action = location.origin + ctx + '/reservation';
      const add = (k,v) => { const i=document.createElement('input'); i.type='hidden'; i.name=k; i.value=v; form.appendChild(i); };

      add('action','confirm_member');
      add('member', JSON.stringify(payload));
      if (selectedCoupon)   add('coupon',   selectedCoupon);
      if (selectedTimeslot) add('timeslot', selectedTimeslot);
      if (date)  add('date',  date);
      if (start) add('start', start);

      document.body.appendChild(form);
      form.submit();
    });
  </script>
</body>
</html>
