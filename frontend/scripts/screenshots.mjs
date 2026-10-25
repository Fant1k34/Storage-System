// Layout smoke test: renders the running app in headless Chrome at several widths, in view and edit mode,
// and writes PNGs to frontend/screenshots/. Usage: TARGET=http://localhost:3000/ WIDTHS=375,768,1024,1366 npm run shots
import { writeFileSync, mkdirSync } from 'node:fs';
import { spawn } from 'node:child_process';

const CHROME = process.env.CHROME ?? '/Applications/Google Chrome.app/Contents/MacOS/Google Chrome';
const URL_ = process.env.TARGET ?? 'http://localhost:3000/';
mkdirSync('screenshots', { recursive: true });
const widths = (process.env.WIDTHS ?? '375,768,1024,1366').split(',').map(Number);

const chrome = spawn(CHROME, ['--headless=new', '--disable-gpu', '--no-first-run', '--remote-debugging-port=9333', '--window-size=1366,900', 'about:blank'], { stdio: 'ignore' });
const sleep = (ms) => new Promise((r) => setTimeout(r, ms));
let wsUrl;
for (let i = 0; i < 40 && !wsUrl; i++) {
  try {
    const list = await (await fetch('http://localhost:9333/json')).json();
    wsUrl = list.find((t) => t.type === 'page')?.webSocketDebuggerUrl;
  } catch { await sleep(250); }
}
const ws = new WebSocket(wsUrl);
await new Promise((r) => (ws.onopen = r));
let id = 0; const pending = new Map();
ws.onmessage = (e) => { const m = JSON.parse(e.data); if (m.id && pending.has(m.id)) { pending.get(m.id)(m); pending.delete(m.id); } };
const send = (method, params = {}) => new Promise((r) => { const i = ++id; pending.set(i, r); ws.send(JSON.stringify({ id: i, method, params })); });
const evaluate = async (expr) => (await send('Runtime.evaluate', { expression: expr, awaitPromise: true, returnByValue: true })).result?.result?.value;

await send('Page.enable'); await send('Runtime.enable');
await send('Page.navigate', { url: URL_ }); await sleep(1200);
for (const w of widths) {
  const h = w < 500 ? 812 : 900;
  await send('Emulation.setDeviceMetricsOverride', { width: w, height: h, deviceScaleFactor: 1, mobile: w < 768 });
  await send('Page.navigate', { url: URL_ });
  await sleep(1500);
  await send('Page.reload');
  await sleep(2500);
  await evaluate(`localStorage.removeItem('sklad.drafts.v1'); 1`);
  const shot = async (name) => {
    const { result } = await send('Page.captureScreenshot', { format: 'png', captureBeyondViewport: true });
    writeFileSync(`screenshots/${name}-${w}.png`, Buffer.from(result.data, 'base64'));
    const overflow = await evaluate(`JSON.stringify({ docW: document.documentElement.scrollWidth, winW: innerWidth, docH: document.documentElement.scrollHeight })`);
    const wide = await evaluate(`(() => { const out=[]; for (const el of document.querySelectorAll('body *')) { const r=el.getBoundingClientRect(); if (r.right > innerWidth + 1 && r.width > 0) out.push(el.tagName.toLowerCase()+'.'+[...el.classList].slice(0,2).join('.')+' right='+Math.round(r.right)+' w='+Math.round(r.width)); } return JSON.stringify(out.slice(0,8)); })()`);
    console.log(`${name}-${w}`, overflow, wide);
  };
  await shot('view');
  // switch to edit and add two rows (one with an unknown item so a hint is visible)
  await evaluate(`(() => { const seg=[...document.querySelectorAll('.ant-segmented-item')].find(e=>/Редактирование|edit/i.test(e.textContent)||e.querySelector('.anticon-edit')); seg?.click(); return !!seg; })()`);
  await sleep(600);
  await evaluate(`(() => { const b=[...document.querySelectorAll('button')].find(e=>e.querySelector('.anticon-plus')); b?.click(); b?.click(); return !!b; })()`);
  await sleep(800);
  await shot('edit');
}
ws.close(); chrome.kill();
