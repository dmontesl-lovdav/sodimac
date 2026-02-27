FROM registrysecaas.azurecr.io/secaas/node:22.17.0-alpine3.21

WORKDIR /usr/src/app
COPY package.json ./
# Bundle app source
COPY . .
EXPOSE 8080
ENTRYPOINT ["npm", "run", "start"]
