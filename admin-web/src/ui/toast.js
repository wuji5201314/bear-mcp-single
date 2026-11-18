import { reactive } from 'vue'

// Minimal sonner-style toast store: no dependency, one host component.
export const toastState = reactive([])

let seed = 0

function push(type, text, duration = 3200) {
  const id = ++seed
  toastState.push({ id, type, text: String(text ?? '') })
  if (duration > 0) {
    window.setTimeout(() => dismiss(id), duration)
  }
  return id
}

export function dismiss(id) {
  const index = toastState.findIndex(item => item.id === id)
  if (index >= 0) {
    toastState.splice(index, 1)
  }
}

export const toast = {
  success: text => push('success', text),
  error: text => push('error', text, 4200),
  warning: text => push('warning', text),
  info: text => push('info', text),
  loading: text => push('loading', text, 2400)
}

export default toast
