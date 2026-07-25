/// <reference types="vite/client" />

interface ImportMetaEnv {
  readonly VITE_API_BASE_URL: string
  readonly VITE_APP_ENV: string
  /** Set to "true" to always send ngrok-skip-browser-warning (optional; also auto-detected from ngrok API URL). */
  readonly VITE_NGROK_SKIP_BROWSER_WARNING?: string
}

interface ImportMeta {
  readonly env: ImportMetaEnv
}
