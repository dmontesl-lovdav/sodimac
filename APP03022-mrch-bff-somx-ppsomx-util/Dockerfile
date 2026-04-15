
FROM node:22-alpine
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
