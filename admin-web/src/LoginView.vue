<script setup>
import { ref } from 'vue'
import { login } from './api'
import Icon from './ui/Icon.vue'

const emit = defineEmits(['success'])
const username = ref('demo-admin')
const password = ref('admin123')
const loading = ref(false)
const error = ref('')

async function submit() {
  if (loading.value) {
    return
  }

  loading.value = true
  error.value = ''

  try {
    const loginUser = await login(username.value, password.value)
    emit('success', loginUser)
  } catch (e) {
    error.value = e.message || '登录失败，请检查用户名和密码'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="auth">
    <aside class="auth-art">
      <div class="row" style="gap: 10px">
        <span class="brand-mark">B</span>
        <div class="brand-text">
          <b>Bear MCP</b>
          <small>Admin Console</small>
        </div>
      </div>

      <div class="col" style="gap: 14px">
        <h1>一个控制台，管理所有 MCP 能力</h1>
        <p>请求配置、动态工具、Prompt、Resource 与 Skill 都在这里编排，配合角色与 Token 双层授权，把能力安全地交给 AI Client。</p>
        <div class="auth-points">
          <div class="auth-point"><Icon name="shield-check" /> 角色工具权限 + Token 能力选择双层校验</div>
          <div class="auth-point"><Icon name="terminal" /> Studio 内联调试，保存即上线</div>
          <div class="auth-point"><Icon name="activity" /> 每次 MCP 调用都有审计留痕</div>
        </div>
      </div>

      <div class="tiny muted">v0.1.0 · 自托管 MCP Server</div>
    </aside>

    <main class="auth-form-wrap">
      <div class="auth-card">
        <div class="col" style="gap: 6px">
          <h2>登录管理控制台</h2>
          <p class="muted">使用管理账号继续，未配置账号可参考 <code class="inline">db/init.sql</code> 的演示数据。</p>
        </div>

        <div v-if="error" class="hint" style="border-color: hsl(var(--destructive) / 0.3); color: hsl(var(--destructive))">
          <Icon name="alert" />
          <span>{{ error }}</span>
        </div>

        <form @submit.prevent="submit">
          <label class="field">
            <span class="field-label">用户名</span>
            <input v-model="username" class="input" autocomplete="username" placeholder="demo-admin" />
          </label>

          <label class="field">
            <span class="field-label">密码</span>
            <input v-model="password" class="input" type="password" autocomplete="current-password" placeholder="••••••" />
          </label>

          <button type="submit" class="btn btn-primary btn-block" :disabled="loading">
            <Icon name="logout" />
            {{ loading ? '登录中…' : '登录管理控制台' }}
          </button>
        </form>

        <p class="tiny muted">登录状态保存在本地 Token 中，Token 由服务端签发并带过期时间。</p>
      </div>
    </main>
  </div>
</template>
