import { chromium } from 'playwright';
import { mkdirSync } from 'fs';
import { join, dirname } from 'path';
import { fileURLToPath } from 'url';

const __dirname = dirname(fileURLToPath(import.meta.url));
const SHOT_DIR = join(__dirname, 'screenshots');
mkdirSync(SHOT_DIR, { recursive: true });

const BASE = 'http://localhost';
const API = 'http://localhost:8080/api/v1';

let TOKEN = '';

async function shot(page, name) {
  const path = join(SHOT_DIR, name);
  await page.screenshot({ path, fullPage: true });
  console.log(`✅ ${name}`);
}

async function loginAPI() {
  const res = await fetch(`${API}/auth/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ username: 'admin', password: 'admin123' }),
  });
  const data = await res.json();
  TOKEN = data.data.token;
  console.log(`🔑 登录成功: ${data.data.user.realName} (${data.data.user.role})`);
  return data;
}

async function callAPI(method, url, body) {
  const opts = { method, headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${TOKEN}` } };
  if (body) opts.body = JSON.stringify(body);
  const res = await fetch(url, opts);
  return res.json();
}

(async () => {
  // API: 登录
  await loginAPI();

  // API: 创建测试计划
  console.log('📝 创建测试数据...');
  const planRes = await callAPI('POST', `${API}/plans`, {
    planType: 'DAILY',
    title: '测试日报-系统功能验证',
    description: '<p>验证系统核心功能流程正常运行</p>',
    priority: 'HIGH',
    startTime: '2026-06-20 09:00:00',
    endTime: '2026-06-20 18:00:00',
    quantTarget: '完成所有功能截图',
    submitDirectly: true,
  });
  const planId = planRes.data?.id || 1;
  console.log(`  计划ID=${planId}, ${planRes.message}`);

  const browser = await chromium.launch({ headless: true });
  const context = await browser.newContext({
    viewport: { width: 1440, height: 900 },
    locale: 'zh-CN',
  });
  const page = await context.newPage();

  // ===== 1. 登录页面（先截图再登录）=====
  console.log('\n📸 截图中...');
  await page.goto(`${BASE}/login`, { waitUntil: 'networkidle' });
  await page.waitForTimeout(600);
  await shot(page, '01-登录页面.png');

  // 执行登录
  await page.fill('input[placeholder="用户名"]', 'admin');
  await page.fill('input[placeholder="密码"]', 'admin123');
  await page.click('button:has-text("登 录")');
  // 等待跳转到计划列表
  await page.waitForTimeout(3000);

  // ===== 2. 计划列表 =====
  await shot(page, '02-计划列表.png');

  // ===== 3. 新建计划页 =====
  await page.goto(`${BASE}/plans/create?type=daily`, { waitUntil: 'networkidle' });
  await page.waitForTimeout(1000);
  await shot(page, '03-新建计划页面.png');

  // ===== 4. 计划详情 + 审批时间线 =====
  await page.goto(`${BASE}/plans/${planId}`, { waitUntil: 'networkidle' });
  await page.waitForTimeout(1200);
  await shot(page, '04-计划详情-审批时间线.png');

  // ===== 5. 成果列表 =====
  await page.goto(`${BASE}/achievements`, { waitUntil: 'networkidle' });
  await page.waitForTimeout(1000);
  await shot(page, '05-成果列表.png');

  // ===== 6. 提交成果页 =====
  await page.goto(`${BASE}/achievements/submit/${planId}`, { waitUntil: 'networkidle' });
  await page.waitForTimeout(1000);
  await shot(page, '06-提交成果页面.png');

  // ===== 7. 个人统计 =====
  await page.goto(`${BASE}/statistics/personal`, { waitUntil: 'networkidle' });
  await page.waitForTimeout(1500);
  await shot(page, '07-个人统计分析.png');

  // ===== 8. 团队统计 =====
  await page.goto(`${BASE}/statistics/team`, { waitUntil: 'networkidle' });
  await page.waitForTimeout(1000);
  await shot(page, '08-团队统计.png');

  // ===== 9. 审批待办 =====
  await page.goto(`${BASE}/approvals/pending`, { waitUntil: 'networkidle' });
  await page.waitForTimeout(1000);
  await shot(page, '09-审批待办.png');

  // ===== 10. 通知列表 =====
  await page.goto(`${BASE}/notifications`, { waitUntil: 'networkidle' });
  await page.waitForTimeout(1000);
  await shot(page, '10-通知列表.png');

  await browser.close();
  console.log('\n🎉 全部 10 张截图完成！');
})();
