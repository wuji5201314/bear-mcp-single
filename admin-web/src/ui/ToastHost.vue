<script setup>
import Icon from './Icon.vue'
import { dismiss, toastState } from './toast'

const ICONS = {
  success: 'check-circle',
  error: 'alert',
  warning: 'alert',
  info: 'info',
  loading: 'refresh'
}
</script>

<template>
  <div class="toast-host">
    <div v-for="item in toastState" :key="item.id" :class="['toast', item.type]" @click="dismiss(item.id)">
      <Icon :name="ICONS[item.type] || 'info'" :size="15" />
      <span>{{ item.text }}</span>
    </div>
  </div>
</template>
