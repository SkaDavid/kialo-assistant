import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
    server: {
      proxy: {
      '/api': {
        target: 'http://kialo-assistant:8080', // Interní název služby v Dockeru
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