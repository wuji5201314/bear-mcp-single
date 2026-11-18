import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// VITE_BASE lets the demo build run under a sub-path (e.g. GitHub Pages /<repo>/).
export default defineConfig({
  base: process.env.VITE_BASE || '/',
  plugins: [vue()],
  server: { port: 5173, proxy: { '/api': 'http://localhost:8090' } }
})
