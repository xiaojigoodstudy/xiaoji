<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'

type MenuKey =
  | 'dashboard'
  | 'radar'
  | 'rules'
  | 'notifications'
  | 'habits'
  | 'pipeline'
  | 'system'

type HealthResponse = {
  code: string
  message: string
  data?: {
    status?: string
    redis?: string
    service?: string
  }
}

const menus: Array<{ key: MenuKey; title: string; desc: string }> = [
  { key: 'dashboard', title: '仪表盘', desc: '运行总览' },
  { key: 'radar', title: '雷达', desc: '信息源与任务' },
  { key: 'rules', title: '规则', desc: '策略与评估' },
  { key: 'notifications', title: '通知', desc: '发送与日志' },
  { key: 'habits', title: '习惯', desc: '事项与打卡' },
  { key: 'pipeline', title: '主链路', desc: '联动触发' },
  { key: 'system', title: '系统', desc: '环境与发布' },
]

const active = ref<MenuKey>('dashboard')
const now = ref(new Date().toLocaleString())
const loadingHealth = ref(false)
const healthStatus = ref('UNKNOWN')
const redisStatus = ref('UNKNOWN')
const serviceName = ref('daily-toolkit-backend')
const healthMessage = ref('等待检测')
const triggerMessage = ref('')

const loginForm = reactive({ username: 'admin', password: '' })
const authed = ref(false)
const currentUser = ref('admin')

const radarSourceForm = reactive({ name: '', type: 'RSS' })
const radarSources = ref([
  { id: 1, name: '行业资讯源', type: 'RSS', enabled: true, updatedAt: '2026-03-27 09:30' },
  { id: 2, name: '竞品监控源', type: 'API', enabled: false, updatedAt: '2026-03-27 10:05' },
])

const radarTaskForm = reactive({ sourceId: 1, name: '每日抓取' })
const radarTasks = ref([
  { id: 1, sourceId: 1, name: '每日抓取', lastRun: '2026-03-27 10:00', status: 'SUCCESS' },
  { id: 2, sourceId: 2, name: '半小时抓取', lastRun: '2026-03-27 10:30', status: 'IDLE' },
])

const ruleForm = reactive({ name: '', expression: 'score > 60' })
const rules = ref([
  { id: 1, name: '高分命中', expression: 'score > 60', enabled: true },
  { id: 2, name: '紧急级别', expression: "level === 'HIGH'", enabled: true },
])
const evaluatePayload = ref('{"score":80}')
const evaluateResult = ref('')

const sendForm = reactive({ channel: 'WECHAT', title: '', content: '' })
const notifyLogs = ref([
  { id: 1, channel: 'WECHAT', title: '启动通知', result: 'SUCCESS', at: '2026-03-27 09:15' },
])

const habitForm = reactive({ name: '' })
const habits = ref([
  { id: 1, name: '晨读 30 分钟', checkins: 2 },
  { id: 2, name: '喝水 8 杯', checkins: 5 },
])

const pipelineForm = reactive({ sourceId: 1, ruleId: 1, channel: 'WECHAT' })
const pipelineLogs = ref<Array<{ id: number; text: string; at: string }>>([])

const todayNotifyCount = computed(() => notifyLogs.value.length)
const todayHabitCount = computed(() => habits.value.reduce((sum, h) => sum + h.checkins, 0))

const healthClass = computed(() => {
  if (healthStatus.value === 'UP' && redisStatus.value === 'UP') return 'ok'
  if (healthStatus.value === 'DOWN') return 'bad'
  return 'warn'
})

const activeMenuTitle = computed(() => menus.find((m) => m.key === active.value)?.title ?? '仪表盘')

const sourceOptions = computed(() => radarSources.value.map((s) => ({ id: s.id, name: s.name })))
const ruleOptions = computed(() => rules.value.map((r) => ({ id: r.id, name: r.name })))

async function refreshHealth() {
  loadingHealth.value = true
  try {
    const resp = await fetch('/api/health', { cache: 'no-store' })
    if (!resp.ok) throw new Error(`HTTP ${resp.status}`)
    const json = (await resp.json()) as HealthResponse
    healthStatus.value = json.data?.status ?? 'UNKNOWN'
    redisStatus.value = json.data?.redis ?? 'UNKNOWN'
    serviceName.value = json.data?.service ?? 'daily-toolkit-backend'
    healthMessage.value = json.message
  } catch (error) {
    healthStatus.value = 'DOWN'
    redisStatus.value = 'UNKNOWN'
    healthMessage.value = error instanceof Error ? error.message : 'health request failed'
  } finally {
    loadingHealth.value = false
  }
}

function login() {
  if (!loginForm.username || !loginForm.password) {
    triggerMessage.value = '请输入用户名和密码'
    return
  }
  authed.value = true
  currentUser.value = loginForm.username
  triggerMessage.value = ''
}

function logout() {
  authed.value = false
  loginForm.password = ''
}

function addRadarSource() {
  if (!radarSourceForm.name.trim()) return
  radarSources.value.unshift({
    id: Date.now(),
    name: radarSourceForm.name.trim(),
    type: radarSourceForm.type,
    enabled: true,
    updatedAt: new Date().toLocaleString(),
  })
  radarSourceForm.name = ''
}

function toggleRadarSource(id: number) {
  const target = radarSources.value.find((s) => s.id === id)
  if (target) {
    target.enabled = !target.enabled
    target.updatedAt = new Date().toLocaleString()
  }
}

function addRadarTask() {
  if (!radarTaskForm.name.trim()) return
  radarTasks.value.unshift({
    id: Date.now(),
    sourceId: Number(radarTaskForm.sourceId),
    name: radarTaskForm.name.trim(),
    lastRun: '-',
    status: 'IDLE',
  })
  radarTaskForm.name = ''
}

function runRadarTask(id: number) {
  const task = radarTasks.value.find((t) => t.id === id)
  if (!task) return
  task.lastRun = new Date().toLocaleString()
  task.status = 'SUCCESS'
}

function addRule() {
  if (!ruleForm.name.trim()) return
  rules.value.unshift({
    id: Date.now(),
    name: ruleForm.name.trim(),
    expression: ruleForm.expression.trim() || 'score > 60',
    enabled: true,
  })
  ruleForm.name = ''
}

function toggleRule(id: number) {
  const target = rules.value.find((r) => r.id === id)
  if (target) target.enabled = !target.enabled
}

function evaluateRule() {
  try {
    const payload = JSON.parse(evaluatePayload.value) as Record<string, unknown>
    const score = Number(payload.score ?? 0)
    evaluateResult.value = score > 60 ? '命中：true（score > 60）' : '命中：false（score <= 60）'
  } catch {
    evaluateResult.value = 'payload 不是合法 JSON'
  }
}

function sendNotification() {
  if (!sendForm.title.trim() || !sendForm.content.trim()) return
  notifyLogs.value.unshift({
    id: Date.now(),
    channel: sendForm.channel,
    title: sendForm.title.trim(),
    result: 'SUCCESS',
    at: new Date().toLocaleString(),
  })
  sendForm.title = ''
  sendForm.content = ''
}

function addHabit() {
  if (!habitForm.name.trim()) return
  habits.value.unshift({ id: Date.now(), name: habitForm.name.trim(), checkins: 0 })
  habitForm.name = ''
}

function checkinHabit(id: number) {
  const target = habits.value.find((h) => h.id === id)
  if (target) target.checkins += 1
}

function triggerPipeline() {
  const source = radarSources.value.find((s) => s.id === Number(pipelineForm.sourceId))
  const rule = rules.value.find((r) => r.id === Number(pipelineForm.ruleId))
  const text = `触发成功：${source?.name ?? '-'} -> ${rule?.name ?? '-'} -> ${pipelineForm.channel}`
  pipelineLogs.value.unshift({ id: Date.now(), text, at: new Date().toLocaleString() })
}

onMounted(() => {
  refreshHealth()
  setInterval(() => {
    now.value = new Date().toLocaleString()
  }, 1000)
})
</script>

<template>
  <div v-if="!authed" class="login-page">
    <div class="login-card">
      <p class="brand">XIAOJI ADMIN</p>
      <h1>管理端原型</h1>
      <p>这是可交互原型页，先验证流程，再接真实接口。</p>
      <label>
        用户名
        <input v-model="loginForm.username" type="text" placeholder="admin" />
      </label>
      <label>
        密码
        <input v-model="loginForm.password" type="password" placeholder="请输入密码" @keyup.enter="login" />
      </label>
      <button @click="login">进入后台</button>
      <p v-if="triggerMessage" class="error">{{ triggerMessage }}</p>
    </div>
  </div>

  <div v-else class="layout">
    <aside class="sidebar">
      <div class="logo">小记后台</div>
      <button
        v-for="menu in menus"
        :key="menu.key"
        class="menu-btn"
        :class="{ active: active === menu.key }"
        @click="active = menu.key"
      >
        <span>{{ menu.title }}</span>
        <small>{{ menu.desc }}</small>
      </button>
    </aside>

    <main class="main">
      <header class="topbar">
        <div>
          <h2>{{ activeMenuTitle }}</h2>
          <p>{{ now }}</p>
        </div>
        <div class="topbar-right">
          <span class="pill" :class="healthClass">Backend {{ healthStatus }} / Redis {{ redisStatus }}</span>
          <span class="pill">{{ currentUser }}</span>
          <button class="ghost" @click="refreshHealth">刷新健康</button>
          <button class="ghost" @click="logout">退出</button>
        </div>
      </header>

      <section v-if="active === 'dashboard'" class="panel-grid">
        <article class="card">
          <h3>服务状态</h3>
          <p><strong>{{ serviceName }}</strong></p>
          <p>{{ healthMessage }}</p>
        </article>
        <article class="card">
          <h3>今日通知</h3>
          <p class="big">{{ todayNotifyCount }}</p>
        </article>
        <article class="card">
          <h3>今日打卡次数</h3>
          <p class="big">{{ todayHabitCount }}</p>
        </article>
        <article class="card wide">
          <h3>快捷入口</h3>
          <div class="quick-links">
            <a href="/api/health" target="_blank" rel="noreferrer">/api/health</a>
            <a href="http://42.193.180.66:8081" target="_blank" rel="noreferrer">管理端 H5</a>
            <a href="http://42.193.180.66:8082" target="_blank" rel="noreferrer">公众号 H5</a>
            <a href="http://42.193.180.66:8083" target="_blank" rel="noreferrer">App H5</a>
          </div>
        </article>
      </section>

      <section v-if="active === 'radar'" class="panel-grid two-col">
        <article class="card">
          <h3>新建信息源</h3>
          <div class="form-row">
            <input v-model="radarSourceForm.name" placeholder="信息源名称" />
            <select v-model="radarSourceForm.type">
              <option value="RSS">RSS</option>
              <option value="API">API</option>
            </select>
            <button @click="addRadarSource">新增</button>
          </div>
          <table>
            <thead>
              <tr><th>名称</th><th>类型</th><th>状态</th><th>操作</th></tr>
            </thead>
            <tbody>
              <tr v-for="item in radarSources" :key="item.id">
                <td>{{ item.name }}</td>
                <td>{{ item.type }}</td>
                <td>{{ item.enabled ? '启用' : '停用' }}</td>
                <td><button class="ghost" @click="toggleRadarSource(item.id)">{{ item.enabled ? '停用' : '启用' }}</button></td>
              </tr>
            </tbody>
          </table>
        </article>

        <article class="card">
          <h3>抓取任务</h3>
          <div class="form-row">
            <select v-model="radarTaskForm.sourceId">
              <option v-for="op in sourceOptions" :key="op.id" :value="op.id">{{ op.name }}</option>
            </select>
            <input v-model="radarTaskForm.name" placeholder="任务名称" />
            <button @click="addRadarTask">新增任务</button>
          </div>
          <table>
            <thead>
              <tr><th>任务</th><th>最近执行</th><th>状态</th><th>操作</th></tr>
            </thead>
            <tbody>
              <tr v-for="task in radarTasks" :key="task.id">
                <td>{{ task.name }}</td>
                <td>{{ task.lastRun }}</td>
                <td>{{ task.status }}</td>
                <td><button class="ghost" @click="runRadarTask(task.id)">手动执行</button></td>
              </tr>
            </tbody>
          </table>
        </article>
      </section>

      <section v-if="active === 'rules'" class="panel-grid two-col">
        <article class="card">
          <h3>规则管理</h3>
          <div class="form-row">
            <input v-model="ruleForm.name" placeholder="规则名称" />
            <input v-model="ruleForm.expression" placeholder="表达式，如 score > 60" />
            <button @click="addRule">新增规则</button>
          </div>
          <table>
            <thead>
              <tr><th>名称</th><th>表达式</th><th>启用</th><th>操作</th></tr>
            </thead>
            <tbody>
              <tr v-for="rule in rules" :key="rule.id">
                <td>{{ rule.name }}</td>
                <td>{{ rule.expression }}</td>
                <td>{{ rule.enabled ? '是' : '否' }}</td>
                <td><button class="ghost" @click="toggleRule(rule.id)">{{ rule.enabled ? '停用' : '启用' }}</button></td>
              </tr>
            </tbody>
          </table>
        </article>
        <article class="card">
          <h3>命中评估</h3>
          <textarea v-model="evaluatePayload" rows="8" />
          <div class="form-row">
            <button @click="evaluateRule">评估</button>
          </div>
          <p class="result">{{ evaluateResult || '点击“评估”后显示结果' }}</p>
        </article>
      </section>

      <section v-if="active === 'notifications'" class="panel-grid two-col">
        <article class="card">
          <h3>发送通知</h3>
          <div class="form-col">
            <select v-model="sendForm.channel">
              <option value="WECHAT">WECHAT</option>
              <option value="APP_PUSH">APP_PUSH</option>
            </select>
            <input v-model="sendForm.title" placeholder="标题" />
            <textarea v-model="sendForm.content" rows="6" placeholder="内容" />
            <button @click="sendNotification">发送</button>
          </div>
        </article>
        <article class="card">
          <h3>通知日志</h3>
          <table>
            <thead>
              <tr><th>时间</th><th>通道</th><th>标题</th><th>结果</th></tr>
            </thead>
            <tbody>
              <tr v-for="log in notifyLogs" :key="log.id">
                <td>{{ log.at }}</td>
                <td>{{ log.channel }}</td>
                <td>{{ log.title }}</td>
                <td>{{ log.result }}</td>
              </tr>
            </tbody>
          </table>
        </article>
      </section>

      <section v-if="active === 'habits'" class="panel-grid two-col">
        <article class="card">
          <h3>事项管理</h3>
          <div class="form-row">
            <input v-model="habitForm.name" placeholder="事项名称" />
            <button @click="addHabit">新增事项</button>
          </div>
          <table>
            <thead>
              <tr><th>事项</th><th>累计打卡</th><th>操作</th></tr>
            </thead>
            <tbody>
              <tr v-for="item in habits" :key="item.id">
                <td>{{ item.name }}</td>
                <td>{{ item.checkins }}</td>
                <td><button class="ghost" @click="checkinHabit(item.id)">打卡</button></td>
              </tr>
            </tbody>
          </table>
        </article>
        <article class="card">
          <h3>统计</h3>
          <p class="big">{{ todayHabitCount }}</p>
          <p>总打卡次数（原型模拟）</p>
        </article>
      </section>

      <section v-if="active === 'pipeline'" class="panel-grid two-col">
        <article class="card">
          <h3>主链路触发</h3>
          <div class="form-col">
            <label>信息源</label>
            <select v-model="pipelineForm.sourceId">
              <option v-for="op in sourceOptions" :key="op.id" :value="op.id">{{ op.name }}</option>
            </select>
            <label>规则</label>
            <select v-model="pipelineForm.ruleId">
              <option v-for="op in ruleOptions" :key="op.id" :value="op.id">{{ op.name }}</option>
            </select>
            <label>通知通道</label>
            <select v-model="pipelineForm.channel">
              <option value="WECHAT">WECHAT</option>
              <option value="APP_PUSH">APP_PUSH</option>
            </select>
            <button @click="triggerPipeline">触发流程</button>
          </div>
        </article>
        <article class="card">
          <h3>触发日志</h3>
          <ul class="logs">
            <li v-for="log in pipelineLogs" :key="log.id">
              <strong>{{ log.at }}</strong>
              <span>{{ log.text }}</span>
            </li>
            <li v-if="pipelineLogs.length === 0">暂无日志</li>
          </ul>
        </article>
      </section>

      <section v-if="active === 'system'" class="panel-grid">
        <article class="card wide">
          <h3>部署与访问信息</h3>
          <ul class="system-list">
            <li>Web 管理端：`http://42.193.180.66/`</li>
            <li>管理端 H5：`http://42.193.180.66:8081/`（或 8001）</li>
            <li>公众号 H5：`http://42.193.180.66:8082/`（或 8002）</li>
            <li>App H5：`http://42.193.180.66:8083/`（或 8003）</li>
            <li>健康接口：`http://42.193.180.66/api/health`</li>
          </ul>
        </article>
      </section>
    </main>
  </div>
</template>

