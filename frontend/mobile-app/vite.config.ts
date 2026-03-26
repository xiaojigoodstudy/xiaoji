import { defineConfig } from 'vite'
import uniModule from '@dcloudio/vite-plugin-uni'

const uniPlugin =
  typeof uniModule === 'function'
    ? uniModule
    : (uniModule as unknown as { default: () => unknown[] }).default

export default defineConfig({
  plugins: [uniPlugin()],
})
