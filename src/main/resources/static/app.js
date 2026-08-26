/* ============================================================================
   Should I buy this? — front end
   Plain ES modules-free JS, same origin as the API, so no CORS and no build.
   State lives in one object; screens are swapped, never routed.
   ========================================================================= */

(function () {
  'use strict';

  /* ── constants mirrored from the API enums ─────────────────────────── */

  var BRACKETS = [
    { id: 'BELOW_25K',       label: 'Below ₹25k',   mid: 20000 },
    { id: 'FROM_25K_TO_50K', label: '₹25k – ₹50k',  mid: 37500 },
    { id: 'FROM_50K_TO_75K', label: '₹50k – ₹75k',  mid: 62500 },
    { id: 'FROM_75K_TO_1L',  label: '₹75k – ₹1L',   mid: 87500 },
    { id: 'FROM_1L_TO_1_5L', label: '₹1L – ₹1.5L',  mid: 125000 },
    { id: 'ABOVE_1_5L',      label: 'Above ₹1.5L',  mid: 175000 }
  ];

  var TYPES = [
    { id: 'NEED',   label: 'Need',   note: 'can’t go without', tone: 'NEED' },
    { id: 'WANT',   label: 'Want',   note: 'would enjoy it',   tone: 'WANT' },
    { id: 'LUXURY', label: 'Luxury', note: 'pure treat',       tone: 'LUXURY' }
  ];

  var USAGE = [
    { id: 'DAILY',   label: 'Daily' },
    { id: 'WEEKLY',  label: 'Weekly' },
    { id: 'MONTHLY', label: 'Monthly' },
    { id: 'RARELY',  label: 'Rarely' }
  ];

  var GLOSS = {
    BUY:  'This one fits. Go ahead.',
    WAIT: 'Not a no — a not-yet.',
    SKIP: 'This would cost you more than it’s worth.'
  };

  /* ── state ─────────────────────────────────────────────────────────── */

  var state = {
    token:   localStorage.getItem('pd.token') || null,
    user:    JSON.parse(localStorage.getItem('pd.user') || 'null'),
    profile: null,
    authMode: 'login',
    profileDraft: {
      bracket: 'FROM_50K_TO_75K',
      savingsMode: 'PERCENTAGE',
      fixedMode: 'AMOUNT'
    },
    ask: { purchaseType: 'WANT', usageFrequency: 'DAILY' }
  };

  /* ── tiny helpers ──────────────────────────────────────────────────── */

  var $  = function (sel, root) { return (root || document).querySelector(sel); };
  var $$ = function (sel, root) { return Array.prototype.slice.call((root || document).querySelectorAll(sel)); };

  var rupees = new Intl.NumberFormat('en-IN', {
    style: 'currency', currency: 'INR', maximumFractionDigits: 0
  });

  function money(v) {
    var n = Number(v);
    return isFinite(n) ? rupees.format(n) : '—';
  }

  function el(tag, cls, text) {
    var n = document.createElement(tag);
    if (cls) n.className = cls;
    if (text != null) n.textContent = text;
    return n;
  }

  /** A ledger row: label ....... value */
  function ledgerRow(key, value, strong) {
    var row = el('div', 'row');
    row.appendChild(el('dt', 'k', key));
    row.appendChild(el('span', 'lead'));
    row.appendChild(el('dd', 'v' + (strong ? ' v-strong' : ''), value));
    return row;
  }

  function show(name) {
    $$('.screen').forEach(function (s) { s.hidden = s.dataset.screen !== name; });
    window.scrollTo({ top: 0, behavior: 'instant' in window ? 'instant' : 'auto' });
  }

  function busy(on, text) {
    $('#veil').hidden = !on;
    if (text) $('#veilText').textContent = text;
  }

  function fail(node, message) {
    node.textContent = message;
    node.hidden = !message;
  }

  /* ── api ───────────────────────────────────────────────────────────── */

  function api(method, path, body) {
    var headers = { 'Content-Type': 'application/json' };
    if (state.token) headers.Authorization = 'Bearer ' + state.token;

    return fetch(path, {
      method: method,
      headers: headers,
      body: body ? JSON.stringify(body) : undefined
    }).then(function (res) {
      return res.text().then(function (raw) {
        var data = null;
        try { data = raw ? JSON.parse(raw) : null; } catch (e) { /* non-JSON body */ }
        if (res.ok) return data;

        var err = new Error((data && data.message) || 'Something went wrong (' + res.status + ')');
        err.status = res.status;
        throw err;
      });
    }, function () {
      throw new Error('Couldn’t reach the API. Is the server running?');
    });
  }

  function setSession(auth) {
    state.token = auth.token;
    state.user  = { name: auth.name, email: auth.email };
    localStorage.setItem('pd.token', auth.token);
    localStorage.setItem('pd.user', JSON.stringify(state.user));
    paintSession();
  }

  function clearSession() {
    state.token = null;
    state.user = null;
    state.profile = null;
    localStorage.removeItem('pd.token');
    localStorage.removeItem('pd.user');
    paintSession();
  }

  function paintSession() {
    var signedIn = !!state.token;
    $('#who').hidden = !signedIn;
    $('#signOut').hidden = !signedIn;
    if (signedIn && state.user) $('#who').textContent = state.user.email;
  }

  /** Anything 401 means the token died — start over rather than loop. */
  function guard(err) {
    if (err.status === 401) {
      clearSession();
      show('gate');
      $('#gateNote').textContent = 'That session expired. Pick up where you left off by signing in again.';
      return true;
    }
    return false;
  }

  /* ── chips ─────────────────────────────────────────────────────────── */

  function buildChips(host, items, selected, onPick) {
    host.textContent = '';
    items.forEach(function (item) {
      var b = el('button', 'chip' + (item.id === selected ? ' is-on' : ''));
      b.type = 'button';
      b.setAttribute('role', 'radio');
      b.setAttribute('aria-checked', item.id === selected ? 'true' : 'false');
      if (item.tone) b.dataset.tone = item.tone;
      b.appendChild(document.createTextNode(item.label));
      if (item.note) b.appendChild(el('small', null, item.note));
      b.addEventListener('click', function () {
        $$('.chip', host).forEach(function (c) {
          c.classList.remove('is-on');
          c.setAttribute('aria-checked', 'false');
        });
        b.classList.add('is-on');
        b.setAttribute('aria-checked', 'true');
        onPick(item.id);
      });
      host.appendChild(b);
    });
  }

  /* ── expense rows ──────────────────────────────────────────────────── */

  function addExpenseRow(name, amount) {
    var row = el('div', 'expense-row');

    var nameWrap = el('label', 'field');
    var nameIn = el('input');
    nameIn.type = 'text';
    nameIn.placeholder = 'Rent';
    nameIn.className = 'exp-name';
    if (name) nameIn.value = name;
    nameWrap.appendChild(nameIn);

    var amtWrap = el('label', 'field');
    var amtBox = el('div', 'amount-in');
    amtBox.appendChild(el('span', 'unit', '₹'));
    var amtIn = el('input');
    amtIn.type = 'number';
    amtIn.min = '0';
    amtIn.step = 'any';
    amtIn.inputMode = 'decimal';
    amtIn.placeholder = '18000';
    amtIn.className = 'exp-amount';
    if (amount != null) amtIn.value = amount;
    amtBox.appendChild(amtIn);
    amtWrap.appendChild(amtBox);

    var x = el('button', 'row-x', '×');
    x.type = 'button';
    x.title = 'Remove this line';
    x.setAttribute('aria-label', 'Remove this line');
    x.addEventListener('click', function () { row.remove(); paintProfilePreview(); });

    row.appendChild(nameWrap);
    row.appendChild(amtWrap);
    row.appendChild(x);
    amtIn.addEventListener('input', paintProfilePreview);

    $('#expenseRows').appendChild(row);
  }

  function readExpenses() {
    return $$('.expense-row').map(function (row) {
      return {
        name: ($('.exp-name', row).value || '').trim(),
        amount: Number($('.exp-amount', row).value)
      };
    }).filter(function (e) { return e.name && isFinite(e.amount); });
  }

  /* ── profile preview ───────────────────────────────────────────────── */

  function bracketMid(id) {
    for (var i = 0; i < BRACKETS.length; i++) if (BRACKETS[i].id === id) return BRACKETS[i].mid;
    return 0;
  }

  function paintProfilePreview() {
    var d = state.profileDraft;
    var income = bracketMid(d.bracket);

    var savingsRaw = Number($('#savingsValue').value) || 0;
    var savings = d.savingsMode === 'PERCENTAGE' ? income * savingsRaw / 100 : savingsRaw;

    var fixed;
    if (d.fixedMode === 'PERCENTAGE') {
      fixed = income * (Number($('#fixedPercentValue').value) || 0) / 100;
    } else {
      fixed = readExpenses().reduce(function (sum, e) { return sum + e.amount; }, 0);
    }

    var left = income - savings - fixed;
    var host = $('#profilePreview');
    host.textContent = '';
    host.appendChild(ledgerRow('Income', money(income)));
    host.appendChild(ledgerRow('Fixed costs', '−' + money(fixed)));
    host.appendChild(ledgerRow('Put away', '−' + money(savings)));

    var total = ledgerRow('Free to spend', money(left), true);
    total.classList.add('row-total');
    host.appendChild(total);
  }

  /* ── history (per browser, not the server) ─────────────────────────── */

  function pastKey() {
    return 'pd.past.' + ((state.user && state.user.email) || 'anon');
  }

  function rememberDecision(d) {
    var list = JSON.parse(localStorage.getItem(pastKey()) || '[]');
    list.unshift({ itemName: d.itemName, price: d.price, verdict: d.verdict });
    localStorage.setItem(pastKey(), JSON.stringify(list.slice(0, 6)));
  }

  function paintPast() {
    var list = JSON.parse(localStorage.getItem(pastKey()) || '[]');
    $('#past').hidden = list.length === 0;
    var host = $('#pastList');
    host.textContent = '';
    list.forEach(function (d) {
      var li = el('li');
      li.appendChild(el('span', 'past-name', d.itemName));
      li.appendChild(el('span', 'past-lead'));
      li.appendChild(el('span', 'past-price', money(d.price)));
      li.appendChild(el('span', 'past-verdict v-' + d.verdict, d.verdict));
      host.appendChild(li);
    });
  }

  /* ── screens ───────────────────────────────────────────────────────── */

  function goAuth(mode) {
    state.authMode = mode;
    var register = mode === 'register';

    $('#authTitle').textContent   = register ? 'Create an account' : 'Sign in';
    $('#authEyebrow').textContent = register ? 'New here' : 'Welcome back';
    $('#authSubmit').textContent  = register ? 'Create account' : 'Sign in';
    $('#nameField').hidden        = !register;
    $('#pwHint').hidden           = !register;
    $('#switchText').textContent  = register ? 'Already have an account?' : 'No account yet?';
    $('#authSwitch').textContent  = register ? 'Sign in' : 'Create one';
    $('#authForm').elements.password.setAttribute(
      'autocomplete', register ? 'new-password' : 'current-password');

    fail($('#authError'), '');
    show('auth');
  }

  function goProfile(existing) {
    var d = state.profileDraft;

    if (existing) {
      d.bracket = existing.incomeBracket || d.bracket;
      d.savingsMode = 'AMOUNT';
      d.fixedMode = 'AMOUNT';
      $('#savingsValue').value = Math.round(Number(existing.savingsTarget) || 0);
    }

    buildChips($('#brackets'), BRACKETS, d.bracket, function (id) {
      d.bracket = id;
      paintProfilePreview();
    });

    setMode('savings', d.savingsMode);
    setMode('fixed', d.fixedMode);

    $('#expenseRows').textContent = '';
    if (existing && existing.fixedExpenses && existing.fixedExpenses.length) {
      existing.fixedExpenses.forEach(function (e) {
        addExpenseRow(e.name, Math.round(Number(e.amount) || 0));
      });
    } else {
      addExpenseRow('Rent', '');
      addExpenseRow('Phone and internet', '');
    }

    fail($('#profileError'), '');
    paintProfilePreview();
    show('profile');
  }

  function setMode(group, mode) {
    var host = $('[data-mode-group=' + group + ']');
    $$('.mode', host).forEach(function (b) { b.classList.toggle('is-on', b.dataset.mode === mode); });

    if (group === 'savings') {
      state.profileDraft.savingsMode = mode;
      $('#savingsUnit').textContent = mode === 'PERCENTAGE' ? '%' : '₹';
    } else {
      state.profileDraft.fixedMode = mode;
      $('#fixedItems').hidden = mode !== 'AMOUNT';
      $('#fixedPercent').hidden = mode !== 'PERCENTAGE';
    }
    paintProfilePreview();
  }

  function goAsk() {
    buildChips($('#purchaseType'), TYPES, state.ask.purchaseType, function (id) {
      state.ask.purchaseType = id;
    });
    buildChips($('#usageFrequency'), USAGE, state.ask.usageFrequency, function (id) {
      state.ask.usageFrequency = id;
    });

    // The sidebar has room for the whole derivation, not just the bottom line.
    var p = state.profile;
    $('#slackLine').hidden = !p;
    if (p) {
      var host = $('#slackLedger');
      host.textContent = '';
      host.appendChild(ledgerRow('Income', money(p.monthlyIncome)));
      host.appendChild(ledgerRow('Fixed costs', '\u2212' + money(p.totalFixedExpenses)));
      host.appendChild(ledgerRow('Put away', '\u2212' + money(p.savingsTarget)));

      var total = ledgerRow('Free to spend', money(p.disposableIncome), true);
      total.classList.add('row-total');
      host.appendChild(total);
    }

    paintPast();
    fail($('#askError'), '');
    show('ask');
  }

  function goVerdict(d) {
    var result = $('#result');
    result.dataset.verdict = d.verdict;

    $('#rItem').textContent    = d.itemName;
    $('#rPrice').textContent   = money(d.price);
    $('#rVerdict').textContent = d.verdict.toLowerCase();
    $('#rGloss').textContent   = GLOSS[d.verdict] || '';
    $('#rScore').textContent   = d.affordScore;
    $('#rProse').textContent   = d.aiExplanation || '';

    // Animate the marker in from 0 so the score reads as a measurement.
    var marker = $('#rMarker');
    marker.style.left = '0%';
    requestAnimationFrame(function () {
      requestAnimationFrame(function () {
        marker.style.left = Math.max(0, Math.min(100, d.affordScore)) + '%';
      });
    });

    var disposable = Number(d.disposableIncome);
    var price = Number(d.price);
    var share = disposable > 0 ? Math.round(price / disposable * 100) : null;

    var math = $('#rMath');
    math.textContent = '';
    math.appendChild(ledgerRow('Free to spend each month', money(disposable)));
    math.appendChild(ledgerRow('This purchase', money(price)));
    if (share != null) {
      math.appendChild(ledgerRow(
        'Share of one month’s slack',
        share + '%',
        true));
    }

    var plan = d.savingsPlan;
    $('#rPlanBlock').hidden = !plan;

    if (plan) {
      var host = $('#rPlan');
      host.textContent = '';
      host.appendChild(ledgerRow('Short by', money(plan.shortfall)));
      host.appendChild(ledgerRow('Set aside monthly', money(plan.monthlySavings)));
      host.appendChild(ledgerRow(
        'Months needed',
        plan.monthsNeeded + (plan.monthsNeeded === 1 ? ' month' : ' months'),
        true));

      var when = plan.targetDate
        ? new Date(plan.targetDate + 'T00:00:00').toLocaleDateString('en-IN', {
            month: 'long', year: 'numeric'
          })
        : null;

      $('#rPlanLine').textContent = when
        ? 'Keep that pace and it stops being a bad idea in ' + when + '.'
        : 'Keep that pace and the shortfall closes.';
    }

    rememberDecision(d);
    show('verdict');
  }

  /* ── after auth: does this account have a profile? ─────────────────── */

  function afterAuth() {
    busy(true, 'Fetching your profile…');
    return api('GET', '/api/users/profile').then(function (profile) {
      state.profile = profile;
      busy(false);
      goAsk();
    }, function (err) {
      busy(false);
      if (guard(err)) return;
      // 400 here just means "no profile yet", which is the expected path
      // for a freshly registered account.
      state.profile = null;
      goProfile(null);
    });
  }

  /* ── wiring ────────────────────────────────────────────────────────── */

  document.addEventListener('click', function (e) {
    var btn = e.target.closest('[data-act]');
    if (!btn) return;
    var act = btn.dataset.act;

    if (act === 'demo') {
      busy(true, 'Setting up a sample profile…');
      api('POST', '/api/auth/demo').then(function (auth) {
        setSession(auth);
        return api('GET', '/api/users/profile');
      }).then(function (profile) {
        state.profile = profile;
        busy(false);
        goAsk();
      }).catch(function (err) {
        busy(false);
        $('#gateNote').textContent = err.message;
      });
    }

    if (act === 'go-login')    goAuth('login');
    if (act === 'go-register') goAuth('register');
    if (act === 'back-gate')   show('gate');
    if (act === 'again')       { $('#askForm').reset(); goAsk(); }
    if (act === 'edit-profile') goProfile(state.profile);
  });

  $('#signOut').addEventListener('click', function () {
    clearSession();
    show('gate');
  });

  $('#authSwitch').addEventListener('click', function () {
    goAuth(state.authMode === 'login' ? 'register' : 'login');
  });

  $('#authForm').addEventListener('submit', function (e) {
    e.preventDefault();
    var f = e.target.elements;
    var register = state.authMode === 'register';

    var body = { email: f.email.value.trim(), password: f.password.value };
    if (register) body.name = f.name.value.trim();

    if (!body.email || !body.password || (register && !body.name)) {
      fail($('#authError'), 'Fill in every field to continue.');
      return;
    }

    fail($('#authError'), '');
    busy(true, register ? 'Creating your account…' : 'Signing you in…');

    api('POST', register ? '/api/auth/register' : '/api/auth/login', body)
      .then(function (auth) {
        setSession(auth);
        busy(false);
        return afterAuth();
      })
      .catch(function (err) {
        busy(false);
        fail($('#authError'), err.message);
      });
  });

  $$('.mode').forEach(function (b) {
    b.addEventListener('click', function () {
      setMode(b.closest('[data-mode-group]').dataset.modeGroup, b.dataset.mode);
    });
  });

  $('#addExpense').addEventListener('click', function () { addExpenseRow('', ''); });
  $('#savingsValue').addEventListener('input', paintProfilePreview);
  $('#fixedPercentValue').addEventListener('input', paintProfilePreview);

  $('#profileForm').addEventListener('submit', function (e) {
    e.preventDefault();
    var d = state.profileDraft;

    var body = {
      incomeBracket: d.bracket,
      savingsInputMode: d.savingsMode,
      savingsValue: Number($('#savingsValue').value) || 0,
      fixedInputMode: d.fixedMode
    };

    if (d.fixedMode === 'PERCENTAGE') {
      body.fixedPercentageValue = Number($('#fixedPercentValue').value) || 0;
    } else {
      body.fixedExpenses = readExpenses();
      if (!body.fixedExpenses.length) {
        fail($('#profileError'), 'Add at least one fixed cost, or switch to a percentage.');
        return;
      }
    }

    fail($('#profileError'), '');
    busy(true, 'Saving your profile…');

    api('PUT', '/api/users/profile', body).then(function (profile) {
      state.profile = profile;
      busy(false);
      goAsk();
    }).catch(function (err) {
      busy(false);
      if (guard(err)) return;
      fail($('#profileError'), err.message);
    });
  });

  $('#askForm').addEventListener('submit', function (e) {
    e.preventDefault();
    var f = e.target.elements;

    var body = {
      itemName: f.itemName.value.trim(),
      price: Number(f.price.value),
      purchaseType: state.ask.purchaseType,
      usageFrequency: state.ask.usageFrequency
    };

    if (!body.itemName || !(body.price > 0)) {
      fail($('#askError'), 'A name and a price above zero are the only things required.');
      return;
    }

    var category = f.category.value.trim();
    var reason = f.reason.value.trim();
    var recurring = f.monthlyRecurringCost.value;
    if (category) body.category = category;
    if (reason) body.reason = reason;
    if (recurring !== '') body.monthlyRecurringCost = Number(recurring);

    fail($('#askError'), '');
    busy(true, 'Doing the arithmetic…');

    api('POST', '/api/decision', body).then(function (d) {
      busy(false);
      goVerdict(d);
    }).catch(function (err) {
      busy(false);
      if (guard(err)) return;
      // The one recoverable case: profile went missing between sessions.
      if (err.status === 400 && /profile/i.test(err.message)) {
        goProfile(null);
        fail($('#profileError'), 'Set this up first and we’ll go straight back to your question.');
        return;
      }
      fail($('#askError'), err.message);
    });
  });

  /* ── boot ──────────────────────────────────────────────────────────── */

  paintSession();
  if (state.token) {
    afterAuth();
  } else {
    show('gate');
  }
})();
