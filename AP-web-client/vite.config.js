import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
    server: {
      proxy: {
      '/api': {
        target: 'http://kialo-assistant:8080',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, '')
      }
    },
    watch: {
      usePolling: true, 
    },
    host: true, 
    strictPort: true,
    port: 5173,
  },
})