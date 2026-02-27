FROM registrysecaas.azurecr.io/secaas/node-dev:20-latest

WORKDIR /usr/src/app
# Install app dependencies
COPY ./node_modules ./node_modules
COPY package.json ./
COPY dist ./dist
COPY tsconfig.json ./

COPY . .

EXPOSE 8080
ENTRYPOINT ["npm", "run", "start"]
