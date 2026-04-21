# ---------- Dependencies stage ----------
FROM registrysecaas.azurecr.io/secaas/node:22.17.0-alpine3.21 AS deps
WORKDIR /usr/src/app

# Copia manifiestos primero para aprovechar caché
COPY package*.json ./

# Instala dependencias prod exactas
RUN npm ci --omit=dev

# ---------- Runtime stage ----------
FROM registrysecaas.azurecr.io/secaas/node:22.17.0-alpine3.21 AS runner
WORKDIR /usr/src/app

ENV NODE_ENV=production

# Usuario no root
USER node

# node_modules de deps + código
COPY --chown=node:node --from=deps /usr/src/app/node_modules ./node_modules
COPY --chown=node:node . .

# Coherente con App.js (default 8080) y Cloud Run
EXPOSE 8080

# Usa npm start (ruta relativa en package.json)
# ENTRYPOINT ["npm", "run", "start"]
ENTRYPOINT ["node", "src/App.js"]