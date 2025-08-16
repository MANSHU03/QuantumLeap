/// <reference types="vite/client" />

interface ImportMetaEnv {
  readonly VITE_API_BASE_URL: string
  readonly VITE_WS_BASE_URL: string
  readonly VITE_APP_NAME: string
  readonly VITE_APP_VERSION: string
  readonly VITE_DEV_MODE: string
  readonly VITE_LOG_LEVEL: string
  readonly VITE_WS_RECONNECT_ATTEMPTS: string
  readonly VITE_WS_RECONNECT_DELAY: string
  readonly VITE_WS_HEARTBEAT_INTERVAL: string
  readonly VITE_CANVAS_WIDTH: string
  readonly VITE_CANVAS_HEIGHT: string
}

interface ImportMeta {
  readonly env: ImportMetaEnv
}
