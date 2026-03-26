<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'

type HealthResponse = {
  code: string
  message: string
  data?: {
    status?: string
    redis?: string
    service?: string
  }
}

const loading = ref(false)
const healthStatus = ref('UNKNOWN')
const healthRedis = ref('UNKNOWN')
const healthMessage = ref('等待检测')

const host = window.location.hostname
const protocol = window.location.protocol

const healthClass = computed(() => {
  if (healthStatus.value === 'UP' && healthRedis.value === 'UP') return 'ok'
  if (healthStatus.value === 'DOWN') return 'bad'
  return 'warn'
})

const entryLinks = computed(() => [
  {
    title: '管理端 Web',
    desc: '当前页面',
    href: `${protocol}//${host}`,
  },
  {
    title: '管理端 H5',
    desc: '移动管理入口',
    href: `${protocol}//${host}:8081`,
  },
  {
    title: '公众号 H5',
    desc: '公众号业务页',
    href: `${protocol}//${host}:8082`,
  },
  {
    title: 'App H5',
    desc: 'uni-app H5 页面',
    href: `${protocol}//${host}:8083`,
  },
  {
    title: '健康检查 API',
    desc: '/api/health',
    href: `${protocol}//${host}/api/health`,
  },
])

async function refreshHealth() {
  loading.value = true
  healthMessage.value = '检测中...'
  try {
    const resp = await fetch('/api/health', { cache: 'no-store' })
    if (!resp.ok) {
      throw new Error(`HTTP ${resp.status}`)
    }

    const json = (await resp.json()) as HealthResponse
    healthStatus.value = json.data?.status ?? 'UNKNOWN'
    healthRedis.value = json.data?.redis ?? 'UNKNOWN'
    healthMessage.value = `${json.message} (${json.data?.service ?? 'service'})`
  } catch (error) {
    healthStatus.value = 'DOWN'
    healthRedis.value = 'UNKNOWN'
    healthMessage.value = error instanceof Error ? error.message : 'request failed'
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  refreshHealth()
})
</script>

<template>
  <div class="page">
    <header class="hero">
      <p class="tag">XIAOJI PLATFORM</p>
      <h1>小记管理控制台</h1>
      <p class="subtitle">
        当前节点：{{ host }} | 后端接口：<code>/api/health</code>
      </p>
      <div class="health-card" :class="healthClass">
        <div>
          <p>后端状态: <strong>{{ healthStatus }}</strong></p>
          <p>Redis 状态: <strong>{{ healthRedis }}</strong></p>
          <p class="hint">{{ healthMessage }}</p>
        </div>
        <button type="button" :disabled="loading" @click="refreshHealth">
          {{ loading ? '刷新中...' : '刷新状态' }}
        </button>
      </div>
    </header>

    <section class="entries">
      <h2>系统入口</h2>
      <div class="grid">
        <a
          v-for="item in entryLinks"
          :key="item.title"
          class="entry-card"
          :href="item.href"
          target="_blank"
          rel="noreferrer"
        >
          <h3>{{ item.title }}</h3>
          <p>{{ item.desc }}</p>
          <span>{{ item.href }}</span>
        </a>
      </div>
    </section>
  </div>
</template>
