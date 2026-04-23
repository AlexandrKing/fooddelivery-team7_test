import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

/**
 * Proxy /api → Spring Boot (default http://localhost:8080) to avoid CORS in local dev.
 * Override: VITE_PROXY_TARGET=http://127.0.0.1:9090 npm run dev
 */
export default defineConfig({
  plugins: [react()],
  test: {
    globals: true,
    environment: 'jsdom',
    setupFiles: './src/test/setupTests.js',
    css: true,
  },
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: process.env.VITE_PROXY_TARGET || 'http://localhost:8080',
        changeOrigin: true,
        configure: (proxy) => {
          proxy.on('proxyRes', (proxyRes) => {
            // Prevent browser basic-auth popup in dev for proxied 401 responses.
            delete proxyRes.headers['www-authenticate'];
          });
        },
      },
    },
  },
});
