const views = [...document.querySelectorAll('.view')];
const navLinks = [...document.querySelectorAll('.nav-link')];
let toastTimer;

function showView(name) {
  views.forEach(view => {
    const active = view.id === name;
    view.classList.toggle('active', active);
    view.hidden = !active;
  });
  navLinks.forEach(link => link.classList.toggle('active', link.dataset.view === name));
  if (name === 'history') loadHistory();
}

document.querySelectorAll('[data-view]').forEach(control => {
  control.addEventListener('click', () => showView(control.dataset.view));
});

async function request(path, options = {}) {
  const response = await fetch(path, {
    credentials: 'same-origin',
    headers: { 'Content-Type': 'application/json', ...(options.headers || {}) },
    ...options
  });
  let data;
  try { data = await response.json(); } catch { data = {}; }
  if (!response.ok) throw new Error(data.error || ('Request failed (' + response.status + ').'));
  return data;
}

function notify(message, isError = false) {
  const toast = document.querySelector('#toast');
  toast.textContent = message;
  toast.classList.toggle('error', isError);
  toast.classList.add('visible');
  clearTimeout(toastTimer);
  toastTimer = setTimeout(() => toast.classList.remove('visible'), 3200);
}

function setInline(element, message, kind = '') {
  element.textContent = message;
  element.className = 'inline-message ' + kind;
}

function setBusy(button, busy, busyText, normalText) {
  button.disabled = busy;
  button.textContent = busy ? busyText : normalText;
}

function cacheMarkup(data) {
  document.querySelector('#cache-capacity').textContent = data.capacity;
  const target = document.querySelector('#cache-entries');
  target.replaceChildren();
  if (!data.entries.length) {
    const empty = document.createElement('p');
    empty.className = 'empty-state';
    empty.textContent = 'The cache is empty. Add a key and value to begin.';
    target.append(empty);
    return;
  }
  const order = document.createElement('span');
  order.className = 'order-label';
  order.textContent = 'MRU';
  target.append(order);
  data.entries.forEach(entry => {
    const chip = document.createElement('div');
    chip.className = 'cache-entry';
    const key = document.createElement('span');
    key.className = 'entry-key';
    key.textContent = entry.key;
    const value = document.createElement('span');
    value.className = 'entry-value';
    value.textContent = entry.value;
    chip.append(key, value);
    target.append(chip);
  });
  const least = document.createElement('span');
  least.className = 'order-label';
  least.textContent = 'LRU';
  target.append(least);
}

async function cacheOperation(operation, button, busyText, capacity) {
  const message = document.querySelector('#cache-message');
  const key = document.querySelector('#cache-key').value.trim();
  const value = document.querySelector('#cache-value').value;
  const normalText = button.textContent;
  setBusy(button, true, busyText, normalText);
  setInline(message, 'Updating cache…');
  try {
    const data = await request('/api/cache', {
      method: 'POST',
      body: JSON.stringify({ operation, key, value, capacity })
    });
    cacheMarkup(data);
    setInline(message, data.message + (data.evictedKey ? (' Evicted: ' + data.evictedKey + '.') : ''), 'success');
  } catch (error) {
    setInline(message, error.message, 'error');
    notify(error.message, true);
  } finally {
    setBusy(button, false, busyText, normalText);
  }
}

document.querySelector('#cache-form').addEventListener('submit', event => {
  event.preventDefault();
  cacheOperation('PUT', event.submitter, 'Saving…');
});
document.querySelector('#cache-get').addEventListener('click', event => {
  cacheOperation('GET', event.currentTarget, 'Checking…');
});
document.querySelector('#cache-reset').addEventListener('click', event => {
  const capacity = Number(document.querySelector('#cache-capacity-input').value);
  if (!Number.isInteger(capacity) || capacity < 1 || capacity > 20) {
    const error = 'Cache capacity must be between 1 and 20.';
    setInline(document.querySelector('#cache-message'), error, 'error');
    return;
  }
  cacheOperation('RESET', event.currentTarget, 'Resetting…', capacity);
});

function renderSimulation(data) {
  document.querySelector('#simulation-results').hidden = false;
  document.querySelector('#hits').textContent = data.hits;
  document.querySelector('#faults').textContent = data.faults;
  document.querySelector('#hit-ratio').textContent = Number(data.hitRatio).toFixed(2) + '%';
  const body = document.querySelector('#simulation-rows');
  body.replaceChildren();
  data.steps.forEach(step => {
    const row = document.createElement('tr');
    [step.position, step.page].forEach(value => {
      const cell = document.createElement('td');
      cell.textContent = value;
      row.append(cell);
    });
    const resultCell = document.createElement('td');
    const pill = document.createElement('span');
    pill.className = 'pill ' + (step.result === 'HIT' ? 'hit' : 'fault');
    pill.textContent = step.result;
    resultCell.append(pill);
    row.append(resultCell);
    const memory = document.createElement('td');
    memory.className = 'memory-state';
    memory.textContent = step.memoryState.join(' → ');
    row.append(memory);
    const eviction = document.createElement('td');
    eviction.textContent = step.evictedPage == null ? '—' : step.evictedPage;
    row.append(eviction);
    body.append(row);
  });
  const final = document.querySelector('#final-memory');
  final.replaceChildren();
  const last = data.steps.at(-1);
  (last?.memoryState || []).forEach((page, index) => {
    const chip = document.createElement('span');
    chip.className = 'memory-chip';
    chip.textContent = (index === 0 ? 'MRU · ' : '') + page;
    final.append(chip);
  });
  if (!last?.memoryState.length) final.textContent = 'No pages loaded.';
}

document.querySelector('#simulation-form').addEventListener('submit', async event => {
  event.preventDefault();
  const form = event.currentTarget;
  if (!form.reportValidity()) return;
  const button = form.querySelector('button[type="submit"]');
  const normalText = button.textContent;
  const busyText = 'Processing and saving simulation…';
  setBusy(button, true, busyText, normalText);
  try {
    const data = await request('/api/simulate', {
      method: 'POST',
      body: JSON.stringify({
        frames: Number(document.querySelector('#frames').value),
        referenceString: document.querySelector('#references').value
      })
    });
    renderSimulation(data);
    notify('Simulation #' + data.id + ' completed and saved.');
  } catch (error) {
    notify(error.message, true);
  } finally {
    setBusy(button, false, busyText, normalText);
  }
});

function historyCell(row, text, className = '') {
  const cell = document.createElement('td');
  cell.textContent = text == null ? '—' : text;
  if (className) cell.className = className;
  row.append(cell);
}

async function loadHistory() {
  const message = document.querySelector('#history-message');
  const body = document.querySelector('#history-rows');
  const empty = document.querySelector('#history-empty');
  setInline(message, 'Loading saved simulations…');
  try {
    const rows = await request('/api/history');
    body.replaceChildren();
    rows.forEach(item => {
      const row = document.createElement('tr');
      historyCell(row, item.id);
      historyCell(row, item.frames);
      historyCell(row, item.referenceString, 'memory-state');
      historyCell(row, item.hits);
      historyCell(row, item.faults);
      historyCell(row, Number(item.hitRatio).toFixed(2) + '%');
      historyCell(row, item.createdAt ? item.createdAt.replace('T', ' ') : '—');
      body.append(row);
    });
    empty.hidden = rows.length > 0;
    setInline(message, rows.length ? (rows.length + ' saved simulation' + (rows.length === 1 ? '' : 's') + '.') : '');
  } catch (error) {
    empty.hidden = true;
    setInline(message, error.message, 'error');
  }
}

document.querySelector('#history-refresh').addEventListener('click', loadHistory);
document.querySelector('#history-clear').addEventListener('click', async event => {
  if (!window.confirm('Clear all saved simulation runs? This will not remove the lrudb database or any other tables.')) return;
  const button = event.currentTarget;
  const normalText = button.textContent;
  setBusy(button, true, 'Clearing…', normalText);
  try {
    const result = await request('/api/history', { method: 'DELETE' });
    notify(result.message);
    await loadHistory();
  } catch (error) {
    notify(error.message, true);
    setInline(document.querySelector('#history-message'), error.message, 'error');
  } finally {
    setBusy(button, false, 'Clearing…', normalText);
  }
});

showView('home');
cacheOperation('RESET', document.querySelector('#cache-reset'), 'Preparing…', 3);
