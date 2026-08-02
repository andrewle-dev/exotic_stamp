import { defineConfig, loadEnv } from 'vite'
import react from '@vitejs/plugin-react'
import tailwindcss from '@tailwindcss/vite'

function normalize(value: string | undefined): string {
  return value?.trim().toLowerCase() ?? ''
}

function trimTrailingSlashes(value: string): string {
  return value.replace(/\/+$/, '')
}

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '')
  const appEnv = normalize(env.VITE_APP_ENV)
  const rawApiBaseUrl = env.VITE_API_BASE_URL?.trim()
  const strictApiBaseUrl =
    normalize(mode) === 'production' ||
    normalize(mode) === 'staging' ||
    appEnv === 'production' ||
    appEnv === 'staging'

  if (strictApiBaseUrl && !rawApiBaseUrl) {
    throw new Error(
      'VITE_API_BASE_URL is required for staging/production builds and must target the HTTPS backend origin.',
    )
  }

  return {
    plugins: [react(), tailwindcss()],
    server: {
      port: 5173,
    },
    define: {
      __APP_BUILD_API_BASE_URL__: JSON.stringify(
        rawApiBaseUrl ? trimTrailingSlashes(rawApiBaseUrl) : undefined,
      ),
    },
  }
})
