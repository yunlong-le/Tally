import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig({
  plugins: [
    react(),
    {
      name: 'remove-module-type',
      transformIndexHtml(html: string) {
        return html
          .replace(/ type="module"/g, '')
          .replace(/ crossorigin/g, '')
          .replace(/<script /g, '<script defer ');
      }
    }
  ],
  base: './',
  build: {
    outDir: '../android/app/src/main/assets/www',
    emptyOutDir: true,
    rollupOptions: {
      output: {
        format: 'iife',
        entryFileNames: 'assets/[name]-[hash].js',
        chunkFileNames: 'assets/[name]-[hash].js',
        assetFileNames: 'assets/[name]-[hash][extname]',
      }
    }
  },
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:3000',
        changeOrigin: true,
      },
    },
  },
});
