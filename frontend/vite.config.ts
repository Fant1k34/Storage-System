import { defineConfig } from 'vitest/config';
import react from '@vitejs/plugin-react';
import { fileURLToPath, URL } from 'node:url';

const BACKEND_URL = process.env.BACKEND_URL ?? 'http://localhost:8080';

// In development Vite proxies /api to Spring; in production server/index.mjs does the same.
export default defineConfig({
  plugins: [react()],
  resolve: { alias: { '@': fileURLToPath(new URL('./src', import.meta.url)) } },
  server: {
    port: 5173,
    proxy: { '/api': { target: BACKEND_URL, changeOrigin: true, rewrite: (p) => p.replace(/^\/api/, '') } },
  },
  // No manualChunks here on purpose: splitting react/antd into separate vendor chunks produced a
  // circular chunk graph and the page died on load with
  // "Cannot read properties of undefined (reading '__SECRET_INTERNALS_DO_NOT_USE_OR_YOU_WILL_BE_FIRED')".
  build: { outDir: 'dist', sourcemap: true, chunkSizeWarningLimit: 1500 },
  test: {
    environment: 'jsdom',
    globals: true,
    setupFiles: ['src/test/setup.ts'],
    css: false,
  },
});
