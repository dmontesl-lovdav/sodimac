FROM registrysecaas.azurecr.io/secaas/node:22.22.3-alpine3.22-dhi

WORKDIR /usr/src/app

# Instala deps con lockfile (si tienes package-lock.json, no lo borres en CI)
COPY package*.json ./
RUN npm ci --omit=dev

# Copia el código
COPY . .

# Expone 8080 (GKE/Cloud Run friendly)
EXPOSE 8080

# Arranque
CMD ["npm", "start"]