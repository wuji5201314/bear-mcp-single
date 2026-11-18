import { createApp } from 'vue'
import './style.css'
import App from './App.vue'

const app = createApp(App)

// VITE_DEMO=1 ships a browser-side mock API so the console can be previewed
// as a static site (GitHub Pages / tunnel). Production builds tree-shake it out.
const boot =
  import.meta.env.VITE_DEMO === '1'
    ? import('./demo/demo-api.js').then((mod) => mod.installDemoApi())
    : Promise.resolve()

boot.then(() => app.mount('#app'))
