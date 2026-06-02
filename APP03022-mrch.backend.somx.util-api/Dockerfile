FROM registrysecaas.azurecr.io/secaas/node:22.22.3-alpine3.22-dhi

WORKDIR /usr/src/app
COPY package.json ./
# Bundle app source
COPY . .

EXPOSE 8082
ENTRYPOINT ["npm", "run", "start"]