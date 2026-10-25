import express from 'express';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
import { createProxyMiddleware } from 'http-proxy-middleware';

// Production server: serves the built SPA from dist/ and proxies /api/* to the Spring backend,
// so browser and API share one origin and the backend needs no CORS configuration.
const PORT = Number(process.env.PORT ?? 3000);
const BACKEND_URL = process.env.BACKEND_URL ?? 'http://localhost:8080';

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const distDir = path.resolve(__dirname, '..', 'dist');

const app = express();
app.disable('x-powered-by');

app.use(
  '/api',
  createProxyMiddleware({
    target: BACKEND_URL,
    changeOrigin: true,
    pathRewrite: { '^/api': '' },
    on: {
      error: (err, _req, res) => {
        console.error('[proxy]', err.message);
        if (res && 'status' in res && !res.headersSent) {
          res.status(502).json({ status: 502, error: 'Bad Gateway', message: `Backend unavailable: ${err.message}` });
        }
      },
    },
  }),
);

app.use(express.static(distDir, { index: false, maxAge: '1h' }));
// Hashed assets can be cached for a long time; index.html must not be.
app.get('*', (_req, res) => {
  res.setHeader('Cache-Control', 'no-store');
  res.sendFile(path.join(distDir, 'index.html'));
});

app.listen(PORT, () => {
  console.log(`sklad frontend: http://localhost:${PORT}  (API -> ${BACKEND_URL})`);
});
