import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
  // Deploiement dans un sous-dossier /chess/ (sur jrix-g.github.io).
  // base s'applique aussi au dev : le serveur tourne alors sur http://localhost:5173/chess/
  base: '/chess/',
  plugins: [react()],
})
